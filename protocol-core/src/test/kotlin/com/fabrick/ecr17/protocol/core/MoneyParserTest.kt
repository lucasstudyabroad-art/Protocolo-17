package com.fabrick.ecr17.protocol.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyParserTest {

    @Test
    fun `parses plain integer as whole euros`() {
        val result = MoneyParser.parse("10") as MoneyParseResult.Valid
        assertEquals(1000L, result.minorUnits)
    }

    @Test
    fun `parses dot decimal separator`() {
        val result = MoneyParser.parse("10.50") as MoneyParseResult.Valid
        assertEquals(1050L, result.minorUnits)
    }

    @Test
    fun `parses comma decimal separator`() {
        val result = MoneyParser.parse("6,50") as MoneyParseResult.Valid
        assertEquals(650L, result.minorUnits)
    }

    @Test
    fun `rejects negative amounts`() {
        assertTrue(MoneyParser.parse("-5") is MoneyParseResult.Invalid)
    }

    @Test
    fun `rejects more than two decimal places`() {
        assertTrue(MoneyParser.parse("10.123") is MoneyParseResult.Invalid)
    }

    @Test
    fun `rejects alphabetic characters`() {
        assertTrue(MoneyParser.parse("10a") is MoneyParseResult.Invalid)
    }

    @Test
    fun `rejects exponent notation`() {
        assertTrue(MoneyParser.parse("1e3") is MoneyParseResult.Invalid)
    }

    @Test
    fun `rejects zero amount`() {
        assertTrue(MoneyParser.parse("0") is MoneyParseResult.Invalid)
        assertTrue(MoneyParser.parse("0.00") is MoneyParseResult.Invalid)
    }

    @Test
    fun `rejects amount exceeding protocol field capacity`() {
        assertTrue(MoneyParser.parse("1000000.00") is MoneyParseResult.Invalid)
    }

    @Test
    fun `rejects multiple decimal separators`() {
        assertTrue(MoneyParser.parse("1.2.3") is MoneyParseResult.Invalid)
        assertTrue(MoneyParser.parse("1,2,3") is MoneyParseResult.Invalid)
    }

    @Test
    fun `toProtocolField formats eight char amount for 6,50`() {
        assertEquals("00000650", MoneyParser.toProtocolField(650L))
    }

    @Test
    fun `fromProtocolField parses back to minor units`() {
        assertEquals(650L, MoneyParser.fromProtocolField("00000650"))
    }

    @Test
    fun `formatForDisplay renders euro symbol with two decimals`() {
        assertEquals("€ 6,50", MoneyParser.formatForDisplay(650L, "€"))
    }

    @Test
    fun `round trip max protocol amount`() {
        val result = MoneyParser.parse("999999.99") as MoneyParseResult.Valid
        assertEquals(99_999_999L, result.minorUnits)
        assertEquals("99999999", MoneyParser.toProtocolField(result.minorUnits))
    }
}
