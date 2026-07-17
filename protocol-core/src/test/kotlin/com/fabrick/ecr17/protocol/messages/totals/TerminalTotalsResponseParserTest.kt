package com.fabrick.ecr17.protocol.messages.totals

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalTotalsResponseParserTest {
    @Test
    fun `parses totals response`() {
        val message = buildString {
            append("12345678"); append("0"); append("T"); append("00")
            append("0000000000012345")
            append("0".repeat(6))
        }
        val response = TerminalTotalsResponseParser.parse(message)
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals(12345L, response.posTotalMinorUnits)
    }
}
