package com.fabrick.ecr17.protocol.messages.lastresult

import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.payment.PaymentResponse
import com.fabrick.ecr17.protocol.messages.refund.RefundResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Ecr17ResponseDispatcherTest {

    private fun paymentApprovedMessage(): String = buildString {
        append("12345678"); append("0"); append("E"); append("00")
        append("0".repeat(15) + "9012")
        append("ICC")
        append("AUTH12")
        append("2111520")
        append("2")
        append("ACQUIRER-01")
        append("000123")
        append("000456")
    }

    private fun refundMessage(): String = buildString {
        append("12345678"); append("0"); append("A"); append("00")
        append("0".repeat(15) + "9012")
        append("ICC")
        append("AUTH12")
        append("ACQUIRER-01")
        append("2111520")
    }

    @Test
    fun `dispatches to payment parser when expected operation is PAYMENT`() {
        val result = Ecr17ResponseDispatcher.parse(paymentApprovedMessage(), OperationType.PAYMENT)
        assertTrue(result is PaymentResponse.Approved)
    }

    @Test
    fun `dispatches to refund parser when expected operation is REFUND`() {
        val result = Ecr17ResponseDispatcher.parse(refundMessage(), OperationType.REFUND)
        assertTrue(result is RefundResponse)
    }

    @Test
    fun `returns null when expected operation is unknown`() {
        assertNull(Ecr17ResponseDispatcher.parse(paymentApprovedMessage(), null))
    }

    @Test
    fun `returns null for operations with no saved last-result shape`() {
        assertNull(Ecr17ResponseDispatcher.parse(paymentApprovedMessage(), OperationType.POS_STATUS))
    }
}
