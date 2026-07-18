package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Pre-authorization closure request — PDF §5.17.1, message code 'c' (lower case). */
data class PreAuthorizationClosureRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
    val amountMinorUnits: Long,
    val receiptText: String = "",
    val originalPreAuthorizationCode: String,
) : Ecr17Request {

    override val operationType = OperationType.PRE_AUTHORIZATION_CLOSURE
    override val messageCode = 'c'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("0000") // pos 20-23, reserved
        append(Ecr17Field.fixedNumeric(amountMinorUnits, 8, "amountMinorUnits")) // pos 24-31
        append(Ecr17Field.fixedAlphanumeric(receiptText, 128, leftAlign = false)) // pos 32-159
        append(Ecr17Field.fixedNumeric(originalPreAuthorizationCode, 9, "originalPreAuthorizationCode")) // pos 160-168
        append("0".repeat(12)) // pos 169-180, reserved
    }
}
