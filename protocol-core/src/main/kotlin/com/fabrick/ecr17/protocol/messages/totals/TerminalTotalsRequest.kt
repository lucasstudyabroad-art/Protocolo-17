package com.fabrick.ecr17.protocol.messages.totals

import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Terminal Totals request — PDF §5.5.1, message code 'T'. No amount involved. */
data class TerminalTotalsRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
) : Ecr17Request {

    override val operationType = OperationType.TERMINAL_TOTALS
    override val messageCode = 'T'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("0000000") // pos 20-26, reserved
    }
}
