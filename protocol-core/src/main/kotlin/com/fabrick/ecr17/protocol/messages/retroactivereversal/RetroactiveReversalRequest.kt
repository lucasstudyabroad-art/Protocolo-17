package com.fabrick.ecr17.protocol.messages.retroactivereversal

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType

/** §5.18.1 pos 24: which prior operation type is being retroactively reversed. */
enum class RetroactiveReversalTransactionType(val code: Char) {
    PURCHASE('0'),
    PRE_AUTHORIZATION('1'),
    PRE_AUTHORIZATION_CLOSURE('2'),
}

/**
 * Retroactive Reversal request — PDF §5.18.1, message code 'Q'.
 *
 * The terminal performs no validation on this data (§5.18: "Terminal doesn't perform any
 * check on the retroactive reversal data received from ECR") — every identifier here must
 * be exactly what the ECR itself recorded for the original transaction being reversed.
 */
data class RetroactiveReversalRequest(
    val terminalId: String,
    val cashRegisterId: String,
    val transactionTypeToReverse: RetroactiveReversalTransactionType,
    val stanToReverse: String,
    /** International card: "C" + last 4 digits (e.g. "C1234"). Italian debit (Pagobancomat): 5-digit ABI code. */
    val cardData: String,
    val acquirerId: String,
    val amountMinorUnits: Long,
    val authorizationCode: String,
    /**
     * Raw date/time as printed on the original receipt. The PDF labels this field
     * "DMMYYhhmm" (9 chars) but gives it length 10 in the same row — an internal
     * inconsistency. This implementation follows the 10-char length (authoritative per the
     * table) and assumes a two-digit day ("DDMMYYhhmm"), consistent with the equivalent
     * field in §5.9.2 (POS status). See `docs/protocol-assumptions.md`.
     */
    val originalTransactionDateTime: String,
) : Ecr17Request {

    override val operationType = OperationType.RETROACTIVE_REVERSAL
    override val messageCode = 'Q'

    override fun applicationMessage(): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId")) // pos 1-8
        append('0') // pos 9
        append(messageCode) // pos 10
        append(Ecr17Field.fixedNumeric(cashRegisterId, 8, "cashRegisterId")) // pos 11-18
        append("00000") // pos 19-23, reserved
        append(transactionTypeToReverse.code) // pos 24
        append(Ecr17Field.fixedNumeric(stanToReverse, 6, "stanToReverse")) // pos 25-30
        append(Ecr17Field.fixedAlphanumeric(cardData, 5, leftAlign = true)) // pos 31-35
        append(Ecr17Field.fixedAlphanumeric(acquirerId, 11, leftAlign = true)) // pos 36-46
        append(Ecr17Field.fixedNumeric(amountMinorUnits, 8, "amountMinorUnits")) // pos 47-54
        append(Ecr17Field.fixedAlphanumeric(authorizationCode, 6, leftAlign = true)) // pos 55-60
        append(Ecr17Field.fixedNumeric(originalTransactionDateTime, 10, "originalTransactionDateTime")) // pos 61-70
        append("0".repeat(10)) // pos 71-80, reserved
    }
}
