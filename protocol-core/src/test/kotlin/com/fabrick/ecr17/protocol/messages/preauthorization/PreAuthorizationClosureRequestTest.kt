package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class PreAuthorizationClosureRequestTest {
    @Test
    fun `builds exact 180 char application message`() {
        val message = PreAuthorizationClosureRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            amountMinorUnits = 2500L,
            originalPreAuthorizationCode = "1234",
        ).applicationMessage()
        assertEquals(180, message.length)
        assertEquals("c", message.protocolField(10, 1))
        assertEquals("00002500", message.protocolField(24, 8))
        assertEquals("000001234", message.protocolField(160, 9))
    }
}
