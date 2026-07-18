package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class PreAuthorizationRequestTest {
    @Test
    fun `builds exact 167 char application message with lower case p code`() {
        val message = PreAuthorizationRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            amountMinorUnits = 5000L,
        ).applicationMessage()
        assertEquals(167, message.length)
        assertEquals("p", message.protocolField(10, 1))
        assertEquals("00005000", message.protocolField(24, 8))
    }
}
