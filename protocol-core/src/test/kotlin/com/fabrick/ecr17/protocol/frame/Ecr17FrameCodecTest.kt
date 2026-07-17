package com.fabrick.ecr17.protocol.frame

import com.fabrick.ecr17.protocol.core.Ecr17Constants
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Ecr17FrameCodecTest {

    private val codec = Ecr17FrameCodec()

    @Test
    fun `encodeAck produces ACK ETX LRC`() {
        val frame = codec.encodeAck()
        assertArrayEquals(byteArrayOf(Ecr17Constants.ACK, Ecr17Constants.ETX, 0x7C), frame)
    }

    @Test
    fun `encodeNak produces NAK ETX LRC`() {
        val frame = codec.encodeNak()
        assertArrayEquals(byteArrayOf(Ecr17Constants.NAK, Ecr17Constants.ETX, 0x7C), frame)
    }

    @Test
    fun `encodeApplication wraps message with STX ETX and computed LRC`() {
        val frame = codec.encodeApplication("s")
        assertEquals(Ecr17Constants.STX, frame[0])
        assertEquals('s'.code.toByte(), frame[1])
        assertEquals(Ecr17Constants.ETX, frame[2])
        val expectedLrc = (0x7F xor 's'.code xor 0x03).toByte()
        assertEquals(expectedLrc, frame[3])
        assertEquals(4, frame.size)
    }

    @Test
    fun `encodeApplication rejects empty message`() {
        assertThrows(IllegalArgumentException::class.java) { codec.encodeApplication("") }
    }

    @Test
    fun `encodeApplication rejects non ascii content`() {
        assertThrows(IllegalArgumentException::class.java) { codec.encodeApplication("café") }
    }

    @Test
    fun `computeApplicationLrc matches encodeApplication trailing byte`() {
        val message = "00000001" + "0" + "s"
        val frame = codec.encodeApplication(message)
        assertEquals(codec.computeApplicationLrc(message), frame.last())
    }
}
