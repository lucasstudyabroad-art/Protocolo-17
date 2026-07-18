package com.fabrick.ecr17.protocol.messages.retroactivereversal

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class RetroactiveReversalRequestTest {
    @Test
    fun `builds exact 80 char application message`() {
        val message = RetroactiveReversalRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            transactionTypeToReverse = RetroactiveReversalTransactionType.PURCHASE,
            stanToReverse = "000123",
            cardData = "C1234",
            acquirerId = "ACQ1",
            amountMinorUnits = 650L,
            authorizationCode = "AUTH12",
            originalTransactionDateTime = "1707261530",
        ).applicationMessage()

        assertEquals(80, message.length)
        assertEquals("Q", message.protocolField(10, 1))
        assertEquals("0", message.protocolField(24, 1))
        assertEquals("000123", message.protocolField(25, 6))
        assertEquals("C1234", message.protocolField(31, 5))
        assertEquals("00000650", message.protocolField(47, 8))
        assertEquals("1707261530", message.protocolField(61, 10))
    }

    @Test
    fun `encodes transaction type to reverse`() {
        val message = RetroactiveReversalRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            transactionTypeToReverse = RetroactiveReversalTransactionType.PRE_AUTHORIZATION_CLOSURE,
            stanToReverse = "000123",
            cardData = "12345",
            acquirerId = "ACQ1",
            amountMinorUnits = 650L,
            authorizationCode = "AUTH12",
            originalTransactionDateTime = "1707261530",
        ).applicationMessage()
        assertEquals("2", message.protocolField(24, 1))
    }
}
