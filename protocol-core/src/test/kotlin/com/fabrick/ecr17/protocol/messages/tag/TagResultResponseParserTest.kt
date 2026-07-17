package com.fabrick.ecr17.protocol.messages.tag

import org.junit.Assert.assertEquals
import org.junit.Test

class TagResultResponseParserTest {

    @Test
    fun `parses variable length additional data using declared length`() {
        val data = "HELLO-GT-DATA"
        val message = buildString {
            append("12345678")
            append("0")
            append("U")
            append("0".repeat(6))
            append(data.length.toString().padStart(3, '0'))
            append(data)
            append("0".repeat(10))
        }
        val response = TagResultResponseParser.parse(message)
        assertEquals(data, response.additionalData)
    }

    @Test
    fun `handles zero length additional data`() {
        val message = buildString {
            append("12345678")
            append("0")
            append("U")
            append("0".repeat(6))
            append("000")
            append("0".repeat(10))
        }
        val response = TagResultResponseParser.parse(message)
        assertEquals("", response.additionalData)
    }
}
