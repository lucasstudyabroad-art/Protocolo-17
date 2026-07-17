package com.fabrick.ecr17.protocol.messages.dll

import org.junit.Assert.assertEquals
import org.junit.Test

class RunDllResponseParserTest {
    @Test
    fun `parses approved run dll response`() {
        val message = buildString {
            append("12345678"); append("0"); append("E"); append("00")
            append("000123")
            append("000456")
            append("2111520")
            append("0".repeat(220))
        }
        val response = RunDllResponseParser.parse(message) as RunDllResponse.Approved
        assertEquals("2111520", response.transactionDateTime)
        assertEquals("000123", response.stan)
    }

    @Test
    fun `parses denied run dll response`() {
        val message = buildString {
            append("12345678"); append("0"); append("E"); append("01")
            append("000123")
            append("000456")
            append("DLL FAILED".padEnd(24).take(24))
            append("0".repeat(11))
        }
        val response = RunDllResponseParser.parse(message) as RunDllResponse.Denied
        assertEquals("DLL FAILED", response.resultDescription)
    }
}
