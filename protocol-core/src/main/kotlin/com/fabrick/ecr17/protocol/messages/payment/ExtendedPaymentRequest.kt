package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.CardPresentMode
import com.fabrick.ecr17.protocol.messages.common.PaymentType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Payment with extended result request — PDF §5.2.1, message code 'X'. Same layout as Basic Payment. */
data class ExtendedPaymentRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
    val cardPresent: CardPresentMode = CardPresentMode.NOT_YET_PRESENT,
    val paymentType: PaymentType = PaymentType.AUTO_RECOGNITION,
    val amountMinorUnits: Long,
    val receiptText: String = "",
) : Ecr17Request {

    override val operationType = OperationType.EXTENDED_PAYMENT
    override val messageCode = 'X'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent))
        append("00")
        append(cardPresent.code)
        append(paymentType.code)
        append(Ecr17Field.fixedNumeric(amountMinorUnits, 8, "amountMinorUnits"))
        append(Ecr17Field.fixedAlphanumeric(receiptText, 128, leftAlign = false))
        append("00000000")
    }
}
