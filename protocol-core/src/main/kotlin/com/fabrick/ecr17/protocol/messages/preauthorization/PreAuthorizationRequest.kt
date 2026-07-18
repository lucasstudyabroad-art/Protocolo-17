package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.CardPresentMode
import com.fabrick.ecr17.protocol.messages.common.PaymentType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Pre-authorization request — PDF §5.15.1, message code 'p' (lower case). Same layout as Basic Payment. */
data class PreAuthorizationRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
    val cardPresent: CardPresentMode = CardPresentMode.NOT_YET_PRESENT,
    val paymentType: PaymentType = PaymentType.AUTO_RECOGNITION,
    val amountMinorUnits: Long,
    val receiptText: String = "",
) : Ecr17Request {

    override val operationType = OperationType.PRE_AUTHORIZATION
    override val messageCode = 'p'

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
