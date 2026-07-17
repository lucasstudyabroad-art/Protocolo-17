package com.fabrick.ecr17.protocol.messages.status

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType

/** POS status request — PDF §5.9.1, message code 's' (lower case). No Cash Register ID. */
data class PosStatusRequest(val terminalId: String) : Ecr17Request {
    override val operationType = OperationType.POS_STATUS
    override val messageCode = 's'

    override fun applicationMessage(): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId")) // pos 1-8
        append('0') // pos 9, reserved
        append(messageCode) // pos 10
    }
}
