package com.fabrick.ecr17.protocol.messages.refund

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class RefundResponseParserTest {
    @Test
    fun `parses refund response`() {
        val message = buildString {
            append("12345678")
            append("0")
            append("A")
            append("00")
            append("0".repeat(15) + "9012")
            append("ICC")
            append("AUTH12")
            append("ACQUIRER-01")
            append("2111520")
        }.let { require(it.length == 58) { "test fixture length ${it.length}" }; it }
        val response = RefundResponseParser.parse(message)
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals("9012", response.maskedPan.last4)
        assertEquals("ACQUIRER-01", response.acquirerId)
        assertEquals("2111520", response.transactionDateTime)
    }
}
