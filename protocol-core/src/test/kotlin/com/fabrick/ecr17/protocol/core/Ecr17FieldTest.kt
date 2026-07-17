package com.fabrick.ecr17.protocol.core

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Ecr17FieldTest {

    @Test
    fun `fixedNumeric pads left with zeros`() {
        assertEquals("00000650", Ecr17Field.fixedNumeric(650L, 8))
    }

    @Test
    fun `fixedNumeric rejects value too large for field`() {
        assertThrows(IllegalArgumentException::class.java) {
            Ecr17Field.fixedNumeric(123456789L, 8)
        }
    }

    @Test
    fun `fixedNumeric rejects negative value`() {
        assertThrows(IllegalArgumentException::class.java) {
            Ecr17Field.fixedNumeric(-1L, 8)
        }
    }

    @Test
    fun `fixedAlphanumeric left aligned pads right with spaces`() {
        assertEquals("ICC   ", Ecr17Field.fixedAlphanumeric("ICC", 6))
    }

    @Test
    fun `fixedAlphanumeric right aligned pads left`() {
        assertEquals("   ICC", Ecr17Field.fixedAlphanumeric("ICC", 6, leftAlign = false))
    }

    @Test
    fun `padLeftZeros preserves leading zeros already present`() {
        assertEquals("00012345", Ecr17Field.padLeftZeros("00012345", 8))
    }

    @Test
    fun `protocolField extracts one-based position correctly`() {
        // Position 10, length 1 -> the message code in a payment request
        val message = "12345678" + "0" + "P" + "87654321"
        assertEquals("P", message.protocolField(10, 1))
        assertEquals("12345678", message.protocolField(1, 8))
        assertEquals("87654321", message.protocolField(11, 8))
    }

    @Test
    fun `protocolField rejects out-of-range position`() {
        assertThrows(IllegalArgumentException::class.java) {
            "short".protocolField(0, 1)
        }
    }

    @Test
    fun `protocolField rejects read past end of string`() {
        assertThrows(IllegalArgumentException::class.java) {
            "short".protocolField(1, 100)
        }
    }

    @Test
    fun `requireNumeric rejects non digit characters`() {
        assertThrows(IllegalArgumentException::class.java) {
            Ecr17Field.requireNumeric("12a4")
        }
    }

    @Test
    fun `requireAscii rejects characters above 127`() {
        assertThrows(IllegalArgumentException::class.java) {
            Ecr17Field.requireAscii("café")
        }
    }

    @Test
    fun `validateExactLength throws on mismatch`() {
        assertThrows(IllegalArgumentException::class.java) {
            Ecr17Field.validateExactLength("abc", 4)
        }
    }
}
