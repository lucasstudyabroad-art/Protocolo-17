package com.fabrick.ecr17.protocol.messages.cardverification

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class CardVerificationResponseParserTest {
    @Test
    fun `parses approved card verification`() {
        val message = buildString {
            append("12345678"); append("0"); append("E"); append("00")
            append("0".repeat(15) + "9012")
            append("ICC")
            append("AUTH12")
            append("2111520")
            append("2")
            append("ACQUIRER-01")
            append("000123")
            append("000456")
        }
        val response = CardVerificationResponseParser.parse(message) as CardVerificationResponse.Approved
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals("9012", response.maskedPan.last4)
    }

    @Test
    fun `parses denied card verification with action code`() {
        val message = buildString {
            append("12345678"); append("0"); append("E"); append("01")
            append("CARD READ ERROR".padEnd(24).take(24))
            append("012")
            append("0".repeat(8))
            append("1")
            append("ACQUIRER-01")
            append("000124")
            append("000457")
        }
        val response = CardVerificationResponseParser.parse(message) as CardVerificationResponse.Denied
        assertEquals("012", response.actionCode)
    }
}
