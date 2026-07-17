package com.fabrick.ecr17.protocol.messages.cardverification

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class CardVerificationRequestTest {
    @Test
    fun `builds exact 39 char application message`() {
        val message = CardVerificationRequest(terminalId = "12345678", cashRegisterId = "87654321").applicationMessage()
        assertEquals(39, message.length)
        assertEquals("H", message.protocolField(10, 1))
        assertEquals("0", message.protocolField(22, 1))
        assertEquals("0", message.protocolField(23, 1))
    }
}
