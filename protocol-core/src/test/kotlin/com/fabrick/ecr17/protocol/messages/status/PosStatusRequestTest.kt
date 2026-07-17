package com.fabrick.ecr17.protocol.messages.status

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class PosStatusRequestTest {
    @Test
    fun `builds exact 10 char application message`() {
        val message = PosStatusRequest(terminalId = "12345678").applicationMessage()
        assertEquals(10, message.length)
        assertEquals("12345678", message.protocolField(1, 8))
        assertEquals("0", message.protocolField(9, 1))
        assertEquals("s", message.protocolField(10, 1))
    }
}
