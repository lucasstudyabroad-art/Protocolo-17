package com.fabrick.ecr17.protocol.messages.receipt

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class ReceiptModeRequestTest {
    @Test
    fun `builds exact 11 char application message`() {
        val message = ReceiptModeRequest(terminalId = "12345678", mode = ReceiptPrintMode.ENABLE_ON_ECR).applicationMessage()
        assertEquals(11, message.length)
        assertEquals("E", message.protocolField(10, 1))
        assertEquals("1", message.protocolField(11, 1))
    }

    @Test
    fun `each documented mode maps to its protocol code`() {
        assertEquals('0', ReceiptPrintMode.DISABLE_ON_ECR.code)
        assertEquals('1', ReceiptPrintMode.ENABLE_ON_ECR.code)
        assertEquals('2', ReceiptPrintMode.ENABLE_BOTH.code)
        assertEquals('3', ReceiptPrintMode.MERCHANT_ON_ECR_CUSTOMER_ON_TERMINAL.code)
    }
}
