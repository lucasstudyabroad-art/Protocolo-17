package com.fabrick.ecr17.protocol.messages.common

import com.fabrick.ecr17.protocol.core.Ecr17Field

/**
 * Builds the 19-character header shared verbatim by most ECR-originated request messages:
 * Terminal ID(1,8) + reserved '0'(9,1) + message code(10,1) + Cash Register ID(11,8) +
 * "additional data present" flag(19,1).
 *
 * Not shared by: POS status (no Cash Register ID at all), Run DLL (no additional-data flag
 * at this position), TAG delivery (different layout entirely), Reversal (additional-data
 * flag is at position 25, after the STAN-to-reverse field), Retroactive Reversal (no
 * additional-data flag).
 */
object StandardRequestHeader {
    fun build(
        terminalId: String,
        messageCode: Char,
        cashRegisterId: String,
        additionalDataPresent: Boolean,
    ): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId"))
        append('0')
        append(messageCode)
        append(Ecr17Field.fixedNumeric(cashRegisterId, 8, "cashRegisterId"))
        append(if (additionalDataPresent) '1' else '0')
    }
}
