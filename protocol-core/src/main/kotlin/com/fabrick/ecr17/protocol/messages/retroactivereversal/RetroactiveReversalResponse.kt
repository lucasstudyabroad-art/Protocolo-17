package com.fabrick.ecr17.protocol.messages.retroactivereversal

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/**
 * Retroactive Reversal response — PDF §5.18.2, message code 'E'.
 *
 * Result "91" (§5.18 prose: "If the terminal is not able to manage this procedure it sends
 * the result message with the transaction result set to '91'") is surfaced via
 * [TransactionResultCode.UNSUPPORTED_PROCEDURE] and parsed using the same field layout as
 * a negative ("01") response.
 */
sealed interface RetroactiveReversalResponse {
    val terminalId: String
    val resultCode: TransactionResultCode
    val trailer: ResponseTrailer
    val actionCode: String

    data class Approved(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val maskedPan: MaskedPan,
        val transactionType: CardTransactionType?,
        val transactionDateTime: String,
        override val trailer: ResponseTrailer,
        override val actionCode: String,
    ) : RetroactiveReversalResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        override val trailer: ResponseTrailer,
        override val actionCode: String,
    ) : RetroactiveReversalResponse
}
