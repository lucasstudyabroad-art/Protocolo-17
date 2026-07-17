package com.fabrick.ecr17.protocol.messages.cardverification

import com.fabrick.ecr17.protocol.messages.common.PaymentType
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Card Verification request — PDF §5.7.1, message code 'H'. */
data class CardVerificationRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
    /** §5.7.1 pos 22: false = standard card verification, true = "Terminale Telematico" verification. */
    val terminaleTelematico: Boolean = false,
    val paymentType: PaymentType = PaymentType.AUTO_RECOGNITION,
) : Ecr17Request {

    override val operationType = OperationType.CARD_VERIFICATION
    override val messageCode = 'H'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("00") // pos 20-21, reserved
        append(if (terminaleTelematico) '1' else '0') // pos 22
        append(paymentType.code) // pos 23
        append("0".repeat(16)) // pos 24-39, reserved
    }
}
