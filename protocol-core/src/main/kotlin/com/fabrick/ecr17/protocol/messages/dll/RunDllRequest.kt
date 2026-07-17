package com.fabrick.ecr17.protocol.messages.dll

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType

/**
 * Run DLL request — PDF §5.8.1, message code 'D'.
 *
 * Additional TAG management cannot be used with this message (§5.8), so unlike most other
 * requests there is no "additional data present" flag at all.
 */
enum class DllTransactionType(val code: Char) {
    MANUAL_DLL('0'),
    FIRST_DLL('1'),
    /** RFU in the PDF ("return Acquirer Data (RFU)") — accepted for completeness, behavior undocumented. */
    RETURN_ACQUIRER_DATA_RFU('2'),
}

data class RunDllRequest(
    val terminalId: String,
    val transactionType: DllTransactionType = DllTransactionType.MANUAL_DLL,
) : Ecr17Request {

    override val operationType = OperationType.RUN_DLL
    override val messageCode = 'D'

    override fun applicationMessage(): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId")) // pos 1-8
        append('0') // pos 9
        append(messageCode) // pos 10
        append("000000") // pos 11-16, reserved
        append(transactionType.code) // pos 17
        append('0') // pos 18, reserved
        append("00") // pos 19-20, reserved
        append("0".repeat(100)) // pos 21-120, reserved
    }
}
