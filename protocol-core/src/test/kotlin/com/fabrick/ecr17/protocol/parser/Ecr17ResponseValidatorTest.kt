package com.fabrick.ecr17.protocol.parser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Ecr17ResponseValidatorTest {

    @Test
    fun `matches when terminal id equals configured id`() {
        assertTrue(Ecr17ResponseValidator.terminalIdMatches("12345678000E00", "12345678"))
    }

    @Test
    fun `matches wildcard 00000000 terminal id`() {
        assertTrue(Ecr17ResponseValidator.terminalIdMatches("00000000000E00", "12345678"))
    }

    @Test
    fun `rejects mismatched terminal id`() {
        assertFalse(Ecr17ResponseValidator.terminalIdMatches("99999999000E00", "12345678"))
    }

    @Test
    fun `rejects message shorter than terminal id field`() {
        assertFalse(Ecr17ResponseValidator.terminalIdMatches("123", "12345678"))
    }
}
