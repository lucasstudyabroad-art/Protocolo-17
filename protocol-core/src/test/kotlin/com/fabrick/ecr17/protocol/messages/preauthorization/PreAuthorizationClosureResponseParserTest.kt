package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreAuthorizationClosureResponseParserTest {

    @Test
    fun `parses approved closure without dcc`() {
        val message = buildString {
            append("12345678"); append("0"); append("c"); append("00")
            append("0".repeat(15) + "9012")
            append("ICC")
            append("AUTH12")
            append("2111520")
            append("2")
            append("ACQUIRER-01")
            append("000123")
            append("000456")
            append("001")
        }
        val response = PreAuthorizationClosureResponseParser.parse(message) as PreAuthorizationClosureResponse.Approved
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertNull(response.currencyExchange)
    }

    @Test
    fun `parses approved closure with dcc`() {
        val message = buildString {
            append("12345678"); append("0"); append("v"); append("00")
            append("0".repeat(15) + "9012")
            append("ICC")
            append("AUTH12")
            append("2111520")
            append("2")
            append("ACQUIRER-01")
            append("000123")
            append("000456")
            append("001")
            append("1") // dcc flag
            append("00011234") // exch rate
            append("USD")
            append("000000000715")
            append("2")
            append("0".repeat(10))
        }
        val response = PreAuthorizationClosureResponseParser.parse(message) as PreAuthorizationClosureResponse.Approved
        val dcc = response.currencyExchange!!
        assertEquals("USD", dcc.currencyCode)
        assertEquals(715L, dcc.transactionAmountInCurrency)
    }

    @Test
    fun `parses denied closure`() {
        val message = buildString {
            append("12345678"); append("0"); append("c"); append("01")
            append("ORIGINAL PREAUTH NOT FOUND".padEnd(24).take(24))
            append("0".repeat(11))
            append("1")
            append("ACQUIRER-01")
            append("000124")
            append("000457")
            append("005")
        }
        val response = PreAuthorizationClosureResponseParser.parse(message) as PreAuthorizationClosureResponse.Denied
        assertEquals("005", response.actionCode)
    }
}
