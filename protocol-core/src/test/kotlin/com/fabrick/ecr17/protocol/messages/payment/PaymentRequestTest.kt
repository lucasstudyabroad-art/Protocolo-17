package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.messages.common.CardPresentMode
import com.fabrick.ecr17.protocol.messages.common.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentRequestTest {

    @Test
    fun `builds exact 167 char application message for a 6,50 euro payment`() {
        val request = PaymentRequest(
            terminalId = "12345678",
            cashRegisterId = "87654321",
            amountMinorUnits = 650L,
        )
        val message = request.applicationMessage()

        assertEquals(167, message.length)
        assertEquals("12345678", message.protocolField(1, 8))
        assertEquals("0", message.protocolField(9, 1))
        assertEquals("P", message.protocolField(10, 1))
        assertEquals("87654321", message.protocolField(11, 8))
        assertEquals("0", message.protocolField(19, 1)) // no additional TAG data
        assertEquals("00", message.protocolField(20, 2))
        assertEquals("0", message.protocolField(22, 1)) // card not yet present
        assertEquals("0", message.protocolField(23, 1)) // auto recognition
        assertEquals("00000650", message.protocolField(24, 8))
        assertEquals(" ".repeat(128), message.protocolField(32, 128))
        assertEquals("00000000", message.protocolField(160, 8))
    }

    @Test
    fun `sets additional data flag when TAG data is present`() {
        val request = PaymentRequest(
            terminalId = "00000001",
            cashRegisterId = "00000001",
            additionalDataPresent = true,
            amountMinorUnits = 100L,
        )
        assertEquals("1", request.applicationMessage().protocolField(19, 1))
    }

    @Test
    fun `card already present and payment type flags are encoded`() {
        val request = PaymentRequest(
            terminalId = "00000001",
            cashRegisterId = "00000001",
            cardPresent = CardPresentMode.ALREADY_PRESENT,
            paymentType = PaymentType.CREDIT_ONLY,
            amountMinorUnits = 100L,
        )
        val message = request.applicationMessage()
        assertEquals("1", message.protocolField(22, 1))
        assertEquals("2", message.protocolField(23, 1))
    }

    @Test
    fun `receipt text is right aligned and left padded with spaces`() {
        val request = PaymentRequest(
            terminalId = "00000001",
            cashRegisterId = "00000001",
            amountMinorUnits = 100L,
            receiptText = "CONTRACT-42",
        )
        val field = request.applicationMessage().protocolField(32, 128)
        assertEquals(128, field.length)
        assertEquals("CONTRACT-42", field.takeLast(11))
        assertEquals(" ".repeat(117), field.take(117))
    }
}
