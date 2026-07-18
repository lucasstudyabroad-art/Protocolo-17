package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class IncrementalAuthorizationRequestTest {
    @Test
    fun `builds exact 176 char application message`() {
        val message = IncrementalAuthorizationRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            amountMinorUnits = 1000L,
            originalPreAuthorizationCode = "1234",
        ).applicationMessage()
        assertEquals(176, message.length)
        assertEquals("i", message.protocolField(10, 1))
        assertEquals("00001000", message.protocolField(24, 8))
        assertEquals("000001234", message.protocolField(160, 9))
    }
}
