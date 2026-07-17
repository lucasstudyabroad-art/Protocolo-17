package com.fabrick.ecr17.protocol.parser

import com.fabrick.ecr17.protocol.frame.Ecr17FrameCodec
import com.fabrick.ecr17.protocol.frame.Ecr17Packet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Ecr17StreamParserTest {

    private val codec = Ecr17FrameCodec()
    private lateinit var parser: Ecr17StreamParser

    @Before
    fun setUp() {
        parser = Ecr17StreamParser()
    }

    @Test
    fun `parses a single complete application frame in one read`() {
        val frame = codec.encodeApplication("s")
        val packets = parser.append(frame)
        assertEquals(1, packets.size)
        val app = packets[0] as Ecr17Packet.Application
        assertEquals("s", app.message)
    }

    @Test
    fun `parses a frame delivered across multiple partial reads`() {
        val frame = codec.encodeApplication("00000001" + "0" + "s")
        val part1 = frame.copyOfRange(0, 3)
        val part2 = frame.copyOfRange(3, 6)
        val part3 = frame.copyOfRange(6, frame.size)

        assertTrue(parser.append(part1).isEmpty())
        assertTrue(parser.append(part2).isEmpty())
        val packets = parser.append(part3)

        assertEquals(1, packets.size)
        assertTrue(packets[0] is Ecr17Packet.Application)
    }

    @Test
    fun `parses multiple frames delivered in a single read`() {
        val ack = codec.encodeAck()
        val app = codec.encodeApplication("s")
        val combined = ack + app

        val packets = parser.append(combined)

        assertEquals(2, packets.size)
        assertTrue(packets[0] is Ecr17Packet.Ack)
        assertTrue(packets[1] is Ecr17Packet.Application)
    }

    @Test
    fun `parses ACK immediately followed by application response in one read`() {
        val ack = codec.encodeAck()
        val response = codec.encodeApplication("0000000100E")
        val combined = ack + response

        val packets = parser.append(combined)

        assertEquals(2, packets.size)
        assertTrue(packets[0] is Ecr17Packet.Ack)
        val appPacket = packets[1] as Ecr17Packet.Application
        assertEquals("0000000100E", appPacket.message)
    }

    @Test
    fun `parses NAK followed by retransmission`() {
        val nak = codec.encodeNak()
        val retransmit = codec.encodeApplication("s")
        val packets = parser.append(nak + retransmit)

        assertEquals(2, packets.size)
        assertTrue(packets[0] is Ecr17Packet.Nak)
        assertTrue(packets[1] is Ecr17Packet.Application)
    }

    @Test
    fun `parses progress packet followed by final response`() {
        val progressMsg = "Contacting host....."
            .let { it.substring(0, 20.coerceAtMost(it.length)).padEnd(20) }
        val progress = byteArrayOf(0x01) + progressMsg.toByteArray(Charsets.US_ASCII) + byteArrayOf(0x04)
        val finalResponse = codec.encodeApplication("0000000100E")

        val packets = parser.append(progress + finalResponse)

        assertEquals(2, packets.size)
        val progressPacket = packets[0] as Ecr17Packet.Progress
        assertEquals(20, progressPacket.message.length)
        assertTrue(packets[1] is Ecr17Packet.Application)
    }

    @Test
    fun `flags invalid LRC without losing subsequent frames`() {
        val goodFrame = codec.encodeApplication("s")
        val corrupted = goodFrame.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 0xFF).toByte() }
        val nextFrame = codec.encodeApplication("s")

        val packets = parser.append(corrupted + nextFrame)

        assertEquals(2, packets.size)
        assertTrue(packets[0] is Ecr17Packet.Invalid)
        assertTrue(packets[1] is Ecr17Packet.Application)
    }

    @Test
    fun `resyncs after a stray unknown control byte`() {
        val stray = byteArrayOf(0x7A) // not STX/ACK/NAK/SOH
        val frame = codec.encodeApplication("s")

        val packets = parser.append(stray + frame)

        assertEquals(2, packets.size)
        assertTrue(packets[0] is Ecr17Packet.Invalid)
        assertTrue(packets[1] is Ecr17Packet.Application)
    }

    @Test
    fun `handles connection closing between packets by leaving partial data buffered`() {
        val frame = codec.encodeApplication("00000001" + "0" + "s")
        val part1 = frame.copyOfRange(0, 2)

        val packets = parser.append(part1)
        assertTrue(packets.isEmpty())
        // Simulate socket close: no more bytes ever arrive; parser must not throw or hang.
        parser.reset()
        assertTrue(parser.append(frame).size == 1)
    }

    @Test
    fun `resets internal buffer on demand`() {
        val frame = codec.encodeApplication("00000001" + "0" + "s")
        parser.append(frame.copyOfRange(0, 3))
        parser.reset()
        val packets = parser.append(frame)
        assertEquals(1, packets.size)
    }

    @Test
    fun `bounded buffer resets and reports invalid when overflowed`() {
        val boundedParser = Ecr17StreamParser(maxBufferSize = 8)
        val junk = ByteArray(20) { 0x41 } // STX never sent, all 'A' bytes are "unexpected" -> would resync one at a time normally
        // Feed a stream that never completes and exceeds the tiny bound to force the overflow branch.
        val incomplete = byteArrayOf(0x02) + ByteArray(30) { 0x41 } // STX + no ETX ever
        val packets = boundedParser.append(incomplete)
        assertTrue(packets.any { it is Ecr17Packet.Invalid })
    }

    @Test
    fun `malformed control frame missing ETX is reported invalid and resynced`() {
        val malformedAck = byteArrayOf(0x06, 0x41, 0x00) // ACK not followed by ETX
        val goodFrame = codec.encodeApplication("s")
        val packets = parser.append(malformedAck + goodFrame)
        assertTrue(packets[0] is Ecr17Packet.Invalid)
        assertTrue(packets.last() is Ecr17Packet.Application)
    }
}
