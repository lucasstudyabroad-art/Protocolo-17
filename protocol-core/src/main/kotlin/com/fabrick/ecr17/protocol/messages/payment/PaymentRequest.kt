package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.CardPresentMode
import com.fabrick.ecr17.protocol.messages.common.PaymentType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Basic Payment request — PDF §5.1.1, message code 'P'. */
data class PaymentRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
    val cardPresent: CardPresentMode = CardPresentMode.NOT_YET_PRESENT,
    val paymentType: PaymentType = PaymentType.AUTO_RECOGNITION,
    val amountMinorUnits: Long,
    val receiptText: String = "",
) : Ecr17Request {

    override val operationType = OperationType.PAYMENT
    override val messageCode = 'P'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("00") // pos 20-21, reserved
        append(cardPresent.code) // pos 22
        append(paymentType.code) // pos 23
        append(Ecr17Field.fixedNumeric(amountMinorUnits, 8, "amountMinorUnits")) // pos 24-31
        append(Ecr17Field.fixedAlphanumeric(receiptText, 128, leftAlign = false)) // pos 32-159
        append("00000000") // pos 160-167, reserved
    }

    companion object {
        const val APPLICATION_MESSAGE_LENGTH = 167
    }
}
