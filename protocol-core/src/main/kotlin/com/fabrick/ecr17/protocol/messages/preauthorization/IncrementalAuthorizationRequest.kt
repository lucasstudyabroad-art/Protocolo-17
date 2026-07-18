package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader

/** Incremental authorization request — PDF §5.16.1, message code 'i' (lower case). */
data class IncrementalAuthorizationRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val additionalDataPresent: Boolean = false,
    val amountMinorUnits: Long,
    val receiptText: String = "",
    /** Original pre-authorization identifier from [PreAuthorizationResponse.Approved.preAuthorizationCode]. */
    val originalPreAuthorizationCode: String,
) : Ecr17Request {

    override val operationType = OperationType.INCREMENTAL_AUTHORIZATION
    override val messageCode = 'i'

    override fun applicationMessage(): String = buildString {
        append(StandardRequestHeader.build(terminalId, messageCode, cashRegisterId, additionalDataPresent)) // pos 1-19
        append("0000") // pos 20-23, reserved
        append(Ecr17Field.fixedNumeric(amountMinorUnits, 8, "amountMinorUnits")) // pos 24-31
        append(Ecr17Field.fixedAlphanumeric(receiptText, 128, leftAlign = false)) // pos 32-159
        append(Ecr17Field.fixedNumeric(originalPreAuthorizationCode, 9, "originalPreAuthorizationCode")) // pos 160-168
        append("00000000") // pos 169-176, reserved
    }
}
