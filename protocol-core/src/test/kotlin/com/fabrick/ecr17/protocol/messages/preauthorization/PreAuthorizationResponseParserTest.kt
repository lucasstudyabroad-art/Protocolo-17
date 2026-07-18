package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class PreAuthorizationResponseParserTest {

    @Test
    fun `parses approved pre-authorization with preauth code`() {
        val message = buildString {
            append("12345678"); append("0"); append("e"); append("00")
            append("0".repeat(15) + "9012") // pan 19
            append("ICC") // 3
            append("AUTH12") // 6
            append("00005000") // preauth amount, 8
            append("000001234") // preauth code, 9 (numeric per spec)
            append("001") // action code, 3
            append("2111520") // date, 7
            append("000") // reserved, 3
            append("2") // card type
            append("ACQUIRER-01") // 11
            append("000123") // stan
            append("000456") // id online
            append("0".repeat(12)) // reserved
        }
        val response = PreAuthorizationResponseParser.parse(message) as PreAuthorizationResponse.Approved
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals(5000L, response.preAuthorizedAmountMinorUnits)
        assertEquals("000001234", response.preAuthorizationCode)
        assertEquals("001", response.actionCode)
    }

    @Test
    fun `parses denied pre-authorization`() {
        val message = buildString {
            append("12345678"); append("0"); append("e"); append("01")
            append("LIMIT EXCEEDED".padEnd(24).take(24))
            append("011")
            append("0".repeat(31))
            append("1")
            append("ACQUIRER-01")
            append("000124")
            append("000457")
            append("0".repeat(12))
        }
        val response = PreAuthorizationResponseParser.parse(message) as PreAuthorizationResponse.Denied
        assertEquals("011", response.actionCode)
        assertEquals("LIMIT EXCEEDED", response.resultDescription)
    }
}
