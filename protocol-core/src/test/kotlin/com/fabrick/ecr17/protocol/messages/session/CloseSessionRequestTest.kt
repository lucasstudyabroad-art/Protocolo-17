package com.fabrick.ecr17.protocol.messages.session

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class CloseSessionRequestTest {
    @Test
    fun `builds exact 26 char application message`() {
        val message = CloseSessionRequest(terminalId = "12345678", cashRegisterId = "87654321").applicationMessage()
        assertEquals(26, message.length)
        assertEquals("C", message.protocolField(10, 1))
        assertEquals("0000000", message.protocolField(20, 7))
    }
}
