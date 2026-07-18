package com.fabrick.ecr17.protocol.messages.ticket

import org.junit.Assert.assertEquals
import org.junit.Test

class TicketLineMessageParserTest {
    @Test
    fun `parses ticket content after the fixed 10 char header`() {
        val message = "12345678" + "0" + "S" + "HELLO WORLD"
        val ticket = TicketLineMessageParser.parse(message)
        assertEquals("12345678", ticket.terminalId)
        assertEquals("HELLO WORLD", ticket.content)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects wrong message code`() {
        TicketLineMessageParser.parse("12345678" + "0" + "Z" + "content")
    }
}
