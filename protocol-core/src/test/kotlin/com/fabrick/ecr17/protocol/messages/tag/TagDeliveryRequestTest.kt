package com.fabrick.ecr17.protocol.messages.tag

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TagDeliveryRequestTest {

    @Test
    fun `builds 36 char prefix with no tag content entries`() {
        val message = TagDeliveryRequest(terminalId = "12345678").applicationMessage()
        assertEquals(36, message.length)
        assertEquals("U", message.protocolField(10, 1))
        assertEquals("000000", message.protocolField(11, 6))
        assertEquals("62", message.protocolField(17, 2))
        assertEquals("DF8D01  ", message.protocolField(19, 8))
        assertEquals("0000", message.protocolField(28, 4))
    }

    @Test
    fun `appends each tag content entry terminated by end-of-field byte`() {
        val message = TagDeliveryRequest(
            terminalId = "12345678",
            tagContents = listOf("ABC", "DEF"),
        ).applicationMessage()

        assertEquals(36 + 4 + 4, message.length)
        assertEquals("ABC" + TagDeliveryRequest.END_OF_FIELD, message.substring(36, 40))
        assertEquals("DEF" + TagDeliveryRequest.END_OF_FIELD, message.substring(40, 44))
    }

    @Test
    fun `rejects more than four tag content entries`() {
        assertThrows(IllegalArgumentException::class.java) {
            TagDeliveryRequest(terminalId = "12345678", tagContents = List(5) { "X" })
        }
    }

    @Test
    fun `rejects tag content entry longer than 100 chars`() {
        assertThrows(IllegalArgumentException::class.java) {
            TagDeliveryRequest(terminalId = "12345678", tagContents = listOf("X".repeat(101)))
        }
    }

    @Test
    fun `accepts asterisk in exclusive tag index`() {
        val message = TagDeliveryRequest(terminalId = "12345678", exclusiveTagIndex = "5*00").applicationMessage()
        assertEquals("5*00", message.protocolField(28, 4))
    }
}
