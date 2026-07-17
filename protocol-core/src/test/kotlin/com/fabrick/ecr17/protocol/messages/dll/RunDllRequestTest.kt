package com.fabrick.ecr17.protocol.messages.dll

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Test

class RunDllRequestTest {
    @Test
    fun `builds exact 120 char application message`() {
        val message = RunDllRequest(terminalId = "12345678", transactionType = DllTransactionType.FIRST_DLL).applicationMessage()
        assertEquals(120, message.length)
        assertEquals("D", message.protocolField(10, 1))
        assertEquals("1", message.protocolField(17, 1))
    }
}
