package com.fabrick.ecr17.protocol.frame

import com.fabrick.ecr17.protocol.core.Ecr17Constants
import java.nio.charset.StandardCharsets

/**
 * Encodes/decodes the three wire-level packet shapes defined in §4 of the PDF:
 * application packet (STX msg ETX LRC), ACK (ACK ETX LRC), NAK (NAK ETX LRC).
 *
 * LRC byte scope (documented in `docs/lrc-implementation-note.md`): the XOR chain covers
 * every byte *after* STX/ACK/NAK up to and including ETX — i.e. STX itself is excluded,
 * ETX is included. This is the conventional interpretation for this class of ECR protocol
 * but is **not** backed by an official worked example in the PDF; see the note for the
 * production-readiness TODO.
 */
class Ecr17FrameCodec(private val lrc: LrcCalculator = Ecr17LrcCalculator()) {

    /** Builds STX + [message] + ETX + LRC. [message] must be pure ASCII (§4.1). */
    fun encodeApplication(message: String): ByteArray {
        require(message.isNotEmpty()) { "application message must not be empty" }
        val msgBytes = message.toByteArray(StandardCharsets.US_ASCII)
        require(String(msgBytes, StandardCharsets.US_ASCII) == message) { "application message must be pure ASCII" }

        val body = msgBytes + Ecr17Constants.ETX
        val lrcByte = lrc.calculate(body)
        return byteArrayOf(Ecr17Constants.STX) + body + lrcByte
    }

    /** Builds ACK + ETX + LRC. */
    fun encodeAck(): ByteArray {
        val body = byteArrayOf(Ecr17Constants.ACK, Ecr17Constants.ETX)
        val lrcByte = lrc.calculate(byteArrayOf(Ecr17Constants.ETX))
        return byteArrayOf(Ecr17Constants.ACK, Ecr17Constants.ETX, lrcByte).also {
            check(it.size == body.size + 1)
        }
    }

    /** Builds NAK + ETX + LRC. */
    fun encodeNak(): ByteArray {
        val lrcByte = lrc.calculate(byteArrayOf(Ecr17Constants.ETX))
        return byteArrayOf(Ecr17Constants.NAK, Ecr17Constants.ETX, lrcByte)
    }

    /** Recomputes the LRC that an application [message] frame should carry. */
    fun computeApplicationLrc(message: String): Byte {
        val msgBytes = message.toByteArray(StandardCharsets.US_ASCII)
        return lrc.calculate(msgBytes + Ecr17Constants.ETX)
    }

    /** Recomputes the LRC for an ACK/NAK frame (both share the same scope: just ETX). */
    fun computeControlLrc(): Byte = lrc.calculate(byteArrayOf(Ecr17Constants.ETX))
}
