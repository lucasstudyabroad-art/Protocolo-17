package com.fabrick.ecr17.protocol.messages.lastresult

import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/**
 * Receipt reprint / "send last result" request — PDF §5.11.1, message code 'G'.
 *
 * The terminal replies with an exact replay of whatever it last saved as a financial
 * result (Payment, Refund, Reversal, Card Verification, Pre-authorization family) —
 * see [Ecr17ResponseDispatcher] for how that reply is decoded.
 */
data class LastResultRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
) : Ecr17Request {

    override val operationType = OperationType.LAST_RESULT
    override val messageCode = 'G'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("000") // pos 20-22, reserved
    }
}
