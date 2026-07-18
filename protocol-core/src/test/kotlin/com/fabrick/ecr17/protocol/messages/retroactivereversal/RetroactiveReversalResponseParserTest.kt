package com.fabrick.ecr17.protocol.messages.retroactivereversal

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class RetroactiveReversalResponseParserTest {

    @Test
    fun `parses approved retroactive reversal`() {
        val message = buildString {
            append("12345678"); append("0"); append("E"); append("00")
            append("0".repeat(15) + "9012")
            append("ICC")
            append("0".repeat(6))
            append("2111520")
            append("2")
            append("ACQUIRER-01")
            append("000123")
            append("000456")
            append("001")
            append("0".repeat(10))
        }
        val response = RetroactiveReversalResponseParser.parse(message) as RetroactiveReversalResponse.Approved
        assertEquals(TransactionResultCode.OK, response.resultCode)
    }

    @Test
    fun `parses unsupported procedure result 91 using denied field layout`() {
        val message = buildString {
            append("12345678"); append("0"); append("E"); append("91")
            append("RETROACTIVE NOT SUPPORTED".padEnd(24).take(24))
            append("0".repeat(11))
            append("1")
            append("ACQUIRER-01")
            append("000124")
            append("000457")
            append("001")
            append("0".repeat(10))
        }
        val response = RetroactiveReversalResponseParser.parse(message) as RetroactiveReversalResponse.Denied
        assertEquals(TransactionResultCode.UNSUPPORTED_PROCEDURE, response.resultCode)
    }
}
