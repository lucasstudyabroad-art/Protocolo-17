package com.fabrick.ecr17.protocol.messages.reversal

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Reversal response — PDF §5.6.2, message code 'E'. */
sealed interface ReversalResponse {
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
    ) : ReversalResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        override val trailer: ResponseTrailer,
        override val actionCode: String,
    ) : ReversalResponse
}
