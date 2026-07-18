package com.fabrick.ecr17.protocol.messages.ticket

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class ReprintTicketRequestTest {
    @Test
    fun `builds exact 22 char application message`() {
        val message = ReprintTicketRequest(terminalId = "12345678", printOnEcr = true, ticketType = TicketType.LAST_SERVICE).applicationMessage()
        assertEquals(22, message.length)
        assertEquals("R", message.protocolField(10, 1))
        assertEquals("1", message.protocolField(11, 1))
        assertEquals("1", message.protocolField(12, 1))
    }
}
