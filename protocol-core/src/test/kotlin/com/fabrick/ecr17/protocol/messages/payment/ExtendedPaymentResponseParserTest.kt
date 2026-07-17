package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtendedPaymentResponseParserTest {

    private fun buildApproved(): String = buildString {
        append("12345678")
        append("0")
        append("E")
        append("00")
        append("0".repeat(15) + "9012")
        append("ICC")
        append("AUTH12")
        append("2111520")
        append("2")
        append("ACQUIRER-01")
        append("000123")
        append("000456")
        append("001")
        append("00000650")
        append("0".repeat(10))
    }

    @Test
    fun `parses approved extended payment response`() {
        val response = ExtendedPaymentResponseParser.parse(buildApproved()) as ExtendedPaymentResponse.Approved
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals("9012", response.maskedPan.last4)
        assertEquals("001", response.actionCode)
        assertEquals(650L, response.amountReceivedByHostMinorUnits)
    }

    @Test
    fun `parses denied extended payment response`() {
        val message = buildString {
            append("12345678")
            append("0")
            append("E")
            append("01")
            append("INSUFFICIENT FUNDS".padEnd(24).take(24))
            append("0".repeat(11))
            append("1")
            append("ACQUIRER-01")
            append("000124")
            append("000457")
            append("005")
            append("00000650")
            append("0".repeat(10))
        }
        val response = ExtendedPaymentResponseParser.parse(message) as ExtendedPaymentResponse.Denied
        assertEquals(TransactionResultCode.KO, response.resultCode)
        assertEquals("005", response.actionCode)
    }
}
