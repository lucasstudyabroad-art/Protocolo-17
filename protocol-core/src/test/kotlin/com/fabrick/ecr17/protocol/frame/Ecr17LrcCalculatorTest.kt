package com.fabrick.ecr17.protocol.frame

import org.junit.Assert.assertEquals
import org.junit.Test

class Ecr17LrcCalculatorTest {

    private val calculator = Ecr17LrcCalculator()

    @Test
    fun `lrc of single ETX byte from seed 0x7F is 0x7C`() {
        val lrc = calculator.calculate(byteArrayOf(0x03))
        assertEquals(0x7C.toByte(), lrc)
    }

    @Test
    fun `lrc of empty byte array equals the seed`() {
        val lrc = calculator.calculate(byteArrayOf())
        assertEquals(0x7F.toByte(), lrc)
    }

    @Test
    fun `lrc is order sensitive xor fold`() {
        val lrcAB = calculator.calculate(byteArrayOf(0x41, 0x42))
        val expected = (0x7F xor 0x41 xor 0x42).toByte()
        assertEquals(expected, lrcAB)
    }

    @Test
    fun `custom seed is honored`() {
        val calc = Ecr17LrcCalculator(seed = 0x00)
        assertEquals(0x03.toByte(), calc.calculate(byteArrayOf(0x03)))
    }
}
