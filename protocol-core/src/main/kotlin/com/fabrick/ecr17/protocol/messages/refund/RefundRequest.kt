package com.fabrick.ecr17.protocol.messages.refund

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Refund request — PDF §5.3.1, message code 'A'. */
data class RefundRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
    val amountMinorUnits: Long,
) : Ecr17Request {

    override val operationType = OperationType.REFUND
    override val messageCode = 'A'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("0000") // pos 20-23, reserved
        append(Ecr17Field.fixedNumeric(amountMinorUnits, 8, "amountMinorUnits")) // pos 24-31
        append("00000000") // pos 32-39, reserved
    }
}
