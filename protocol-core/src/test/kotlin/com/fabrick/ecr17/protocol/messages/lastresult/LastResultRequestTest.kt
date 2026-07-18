package com.fabrick.ecr17.protocol.messages.lastresult

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class LastResultRequestTest {
    @Test
    fun `builds exact 22 char application message`() {
        val message = LastResultRequest(terminalId = "12345678", cashRegisterId = "87654321").applicationMessage()
        assertEquals(22, message.length)
        assertEquals("G", message.protocolField(10, 1))
    }
}
