package com.fabrick.ecr17.protocol.messages.totals

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalTotalsRequestTest {
    @Test
    fun `builds exact 26 char application message`() {
        val message = TerminalTotalsRequest(terminalId = "12345678", cashRegisterId = "87654321").applicationMessage()
        assertEquals(26, message.length)
        assertEquals("T", message.protocolField(10, 1))
    }
}
