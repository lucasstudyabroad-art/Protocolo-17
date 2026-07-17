package com.fabrick.ecr17.protocol.messages.reversal

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class ReversalRequestTest {
    @Test
    fun `builds exact 26 char application message with STAN`() {
        val message = ReversalRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            stanToReverse = "000123",
        ).applicationMessage()
        assertEquals(26, message.length)
        assertEquals("S", message.protocolField(10, 1))
        assertEquals("000123", message.protocolField(19, 6))
        assertEquals("0", message.protocolField(25, 1))
        assertEquals("0", message.protocolField(26, 1))
    }

    @Test
    fun `defaults STAN to all zeros meaning no check`() {
        val message = ReversalRequest(terminalId = "12345678", cashRegisterId = "87654321").applicationMessage()
        assertEquals("000000", message.protocolField(19, 6))
    }
}
