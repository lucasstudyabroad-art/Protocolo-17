package com.fabrick.ecr17.protocol.messages.reversal

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType

/**
 * Reversal request — PDF §5.6.1, message code 'S'.
 *
 * Note: field layout differs from [com.fabrick.ecr17.protocol.messages.common.StandardRequestHeader]
 * — the "additional data present" flag is at position 25 here (after the STAN-to-reverse
 * field), not position 19.
 */
data class ReversalRequest(
    val terminalId: String,
    val cashRegisterId: String,
    /** STAN of the transaction to reverse. "000000" = no check; terminal verifies it matches the last payment STAN otherwise. */
    val stanToReverse: String = "000000",
    val additionalDataPresent: Boolean = false,
    /**
     * §5.6.1 pos 26: '0' = terminal requires the same card (only value handled by PAX
     * terminals). '1'/'2' are documented in the PDF but explicitly called out as "NOT
     * MANAGED by Pax Terminal" — exposed here for spec completeness, not recommended.
     */
    val cardRequirementFlag: Char = '0',
) : Ecr17Request {

    override val operationType = OperationType.REVERSAL
    override val messageCode = 'S'

    override fun applicationMessage(): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId"))
        append('0')
        append(messageCode)
        append(Ecr17Field.fixedNumeric(cashRegisterId, 8, "cashRegisterId"))
        append(Ecr17Field.fixedNumeric(stanToReverse, 6, "stanToReverse")) // pos 19-24
        append(if (additionalDataPresent) '1' else '0') // pos 25
        append(cardRequirementFlag) // pos 26
    }
}
