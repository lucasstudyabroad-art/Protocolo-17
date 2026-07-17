package com.fabrick.ecr17.protocol.messages.reversal

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class ReversalResponseParserTest {
    @Test
    fun `parses approved reversal`() {
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
        val response = ReversalResponseParser.parse(message) as ReversalResponse.Approved
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals("9012", response.maskedPan.last4)
        assertEquals("001", response.actionCode)
    }
}
