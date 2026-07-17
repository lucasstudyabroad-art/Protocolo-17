package com.fabrick.ecr17.protocol.messages.session

import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Close Session request — PDF §5.4.1, message code 'C'. No amount involved. */
data class CloseSessionRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
) : Ecr17Request {

    override val operationType = OperationType.CLOSE_SESSION
    override val messageCode = 'C'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("0000000") // pos 20-26, reserved
    }
}
