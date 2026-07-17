package com.fabrick.ecr17.protocol.frame

import com.fabrick.ecr17.protocol.core.Ecr17Constants

/**
 * Computes the LRC (Longitudinal Redundancy Check) control byte defined in §2.1 of the PDF:
 * "LRC character is computed executing an exclusive OR on any message byte, using as base
 * value 0x7F."
 *
 * This interface is intentionally byte-scope-agnostic: it XORs exactly the bytes it is given,
 * starting from the seed. The decision of *which* bytes of a frame participate in the LRC is
 * made by [Ecr17FrameCodec], not here — see `docs/lrc-implementation-note.md` for the
 * documented assumption (message + ETX, STX excluded) and its certification caveat.
 */
interface LrcCalculator {
    fun calculate(bytes: ByteArray): Byte
}

class Ecr17LrcCalculator(
    private val seed: Byte = Ecr17Constants.LRC_INITIAL_VALUE,
) : LrcCalculator {
    override fun calculate(bytes: ByteArray): Byte {
        var lrc = seed
        for (b in bytes) {
            lrc = (lrc.toInt() xor b.toInt()).toByte()
        }
        return lrc
    }
}
