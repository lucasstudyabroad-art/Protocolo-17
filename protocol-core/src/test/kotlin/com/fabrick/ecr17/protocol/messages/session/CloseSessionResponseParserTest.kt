package com.fabrick.ecr17.protocol.messages.session

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class CloseSessionResponseParserTest {
    @Test
    fun `parses approved close session with totals`() {
        val message = buildString {
            append("12345678"); append("0"); append("C"); append("00")
            append("0000000000012345") // pos total, 16 chars
            append("0000000000067890") // host total, 16 chars
        }
        val response = CloseSessionResponseParser.parse(message) as CloseSessionResponse.Approved
        assertEquals(12345L, response.posTotalMinorUnits)
        assertEquals(67890L, response.hostTotalMinorUnits)
    }

    @Test
    fun `parses denied close session`() {
        val message = buildString {
            append("12345678"); append("0"); append("C"); append("01")
            append("SESSION ALREADY CLOSED".padEnd(19).take(19))
            append("007")
            append("0".repeat(10))
        }
        val response = CloseSessionResponseParser.parse(message) as CloseSessionResponse.Denied
        assertEquals(TransactionResultCode.KO, response.resultCode)
        assertEquals("007", response.actionCode)
    }
}
