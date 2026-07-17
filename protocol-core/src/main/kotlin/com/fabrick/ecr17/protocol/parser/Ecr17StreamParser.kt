package com.fabrick.ecr17.protocol.parser

import com.fabrick.ecr17.protocol.core.Ecr17Constants
import com.fabrick.ecr17.protocol.frame.Ecr17FrameCodec
import com.fabrick.ecr17.protocol.frame.Ecr17Packet
import com.fabrick.ecr17.protocol.frame.LrcCalculator
import com.fabrick.ecr17.protocol.frame.Ecr17LrcCalculator
import java.nio.charset.StandardCharsets

/**
 * Stateful byte-stream de-framer for the ECR17 protocol.
 *
 * TCP is a byte stream: a single `Socket.read()` may deliver a partial frame, multiple
 * frames, or an ACK immediately followed by the application response. This parser buffers
 * incoming bytes and extracts as many complete [Ecr17Packet]s as are currently available,
 * leaving any trailing partial frame buffered for the next [append] call.
 *
 * Recovery strategy for malformed data: a single leading byte that doesn't match any known
 * control byte (STX/ACK/NAK/SOH) is emitted as [Ecr17Packet.Invalid] and dropped, then
 * parsing resumes at the next byte (byte-at-a-time resync) rather than discarding the whole
 * buffer, so genuine frames following noise are still recovered.
 */
class Ecr17StreamParser(
    private val lrc: LrcCalculator = Ecr17LrcCalculator(),
    private val maxBufferSize: Int = 16 * 1024,
) {
    private val codec = Ecr17FrameCodec(lrc)
    private val buffer = ArrayDeque<Byte>()

    fun append(bytes: ByteArray): List<Ecr17Packet> {
        buffer.addAll(bytes.toList())
        val packets = mutableListOf<Ecr17Packet>()

        while (true) {
            val packet = tryExtractOne() ?: break
            packets.add(packet)
        }

        if (buffer.size > maxBufferSize) {
            packets.add(Ecr17Packet.Invalid("buffer exceeded $maxBufferSize bytes without a complete frame; buffer reset", buffer.toByteArray()))
            buffer.clear()
        }

        return packets
    }

    fun reset() {
        buffer.clear()
    }

    /** Returns one packet and consumes its bytes from [buffer], or null if more data is needed. */
    private fun tryExtractOne(): Ecr17Packet? {
        if (buffer.isEmpty()) return null

        return when (buffer.first()) {
            Ecr17Constants.STX -> tryExtractApplication()
            Ecr17Constants.ACK -> tryExtractControl(Ecr17Constants.ACK) { raw -> Ecr17Packet.Ack(raw) }
            Ecr17Constants.NAK -> tryExtractControl(Ecr17Constants.NAK) { raw -> Ecr17Packet.Nak(raw) }
            Ecr17Constants.SOH -> tryExtractProgress()
            else -> {
                val stray = buffer.removeFirst()
                Ecr17Packet.Invalid("unexpected leading byte 0x%02X".format(stray), byteArrayOf(stray))
            }
        }
    }

    private fun tryExtractApplication(): Ecr17Packet? {
        // Find ETX after the leading STX (index 0). Message content is never expected to
        // contain a literal ETX byte, so the first 0x03 after position 0 terminates it.
        val etxIndex = buffer.drop(1).indexOfFirst { it == Ecr17Constants.ETX }
        if (etxIndex < 0) return null // no ETX yet: frame still incomplete
        val etxPos = etxIndex + 1 // position within buffer, STX is at 0
        // total frame length = STX(1) + message(etxPos-1 bytes) + ETX(1) + LRC(1) = etxPos + 2
        val totalLength = etxPos + 2
        if (buffer.size < totalLength) return null // LRC byte not arrived yet

        val frame = (0 until totalLength).map { buffer.elementAt(it) }.toByteArray()
        repeat(totalLength) { buffer.removeFirst() }

        val messageBytes = frame.copyOfRange(1, etxPos)
        val etxByte = frame[etxPos]
        val lrcByte = frame[totalLength - 1]
        check(etxByte == Ecr17Constants.ETX)

        val expectedLrc = codec.computeApplicationLrc(String(messageBytes, StandardCharsets.US_ASCII))
        return if (expectedLrc == lrcByte) {
            Ecr17Packet.Application(String(messageBytes, StandardCharsets.US_ASCII), frame)
        } else {
            Ecr17Packet.Invalid("LRC mismatch: expected 0x%02X got 0x%02X".format(expectedLrc, lrcByte), frame)
        }
    }

    private inline fun tryExtractControl(controlByte: Byte, build: (ByteArray) -> Ecr17Packet): Ecr17Packet? {
        val totalLength = 3 // control + ETX + LRC
        if (buffer.size < totalLength) return null
        val frame = (0 until totalLength).map { buffer.elementAt(it) }.toByteArray()

        if (frame[1] != Ecr17Constants.ETX) {
            // Malformed: resync by dropping just the leading control byte.
            buffer.removeFirst()
            return Ecr17Packet.Invalid("expected ETX after control byte 0x%02X".format(controlByte), byteArrayOf(frame[0]))
        }

        repeat(totalLength) { buffer.removeFirst() }
        val expectedLrc = codec.computeControlLrc()
        return if (expectedLrc == frame[2]) {
            build(frame)
        } else {
            Ecr17Packet.Invalid("LRC mismatch on control frame: expected 0x%02X got 0x%02X".format(expectedLrc, frame[2]), frame)
        }
    }

    private fun tryExtractProgress(): Ecr17Packet? {
        val totalLength = 1 + Ecr17Constants.PROGRESS_MESSAGE_LENGTH + 1 // SOH + 20 + EOT
        if (buffer.size < totalLength) return null
        val frame = (0 until totalLength).map { buffer.elementAt(it) }.toByteArray()

        val eotByte = frame[totalLength - 1]
        if (eotByte != Ecr17Constants.EOT) {
            // Malformed: resync by dropping just the SOH byte, try again from the next byte.
            buffer.removeFirst()
            return Ecr17Packet.Invalid("expected EOT terminating progress packet, got 0x%02X".format(eotByte), byteArrayOf(frame[0]))
        }

        repeat(totalLength) { buffer.removeFirst() }
        val messageBytes = frame.copyOfRange(1, totalLength - 1)
        return Ecr17Packet.Progress(String(messageBytes, StandardCharsets.US_ASCII), frame)
    }
}
