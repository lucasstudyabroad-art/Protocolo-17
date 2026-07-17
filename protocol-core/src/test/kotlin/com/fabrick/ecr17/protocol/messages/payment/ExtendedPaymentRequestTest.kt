package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtendedPaymentRequestTest {

    @Test
    fun `builds exact 167 char application message with X code`() {
        val request = ExtendedPaymentRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            amountMinorUnits = 650L,
        )
        val message = request.applicationMessage()
        assertEquals(167, message.length)
        assertEquals("X", message.protocolField(10, 1))
        assertEquals("00000650", message.protocolField(24, 8))
    }
}
