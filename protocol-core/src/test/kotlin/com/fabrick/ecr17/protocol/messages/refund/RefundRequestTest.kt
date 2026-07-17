package com.fabrick.ecr17.protocol.messages.refund

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class RefundRequestTest {
    @Test
    fun `builds exact 39 char application message`() {
        val request = RefundRequest(terminalId = "12345678", cashRegisterId = "87654321", amountMinorUnits = 650L)
        val message = request.applicationMessage()
        assertEquals(39, message.length)
        assertEquals("A", message.protocolField(10, 1))
        assertEquals("0", message.protocolField(19, 1))
        assertEquals("00000650", message.protocolField(24, 8))
        assertEquals("00000000", message.protocolField(32, 8))
    }
}
