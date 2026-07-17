package com.fabrick.ecr17.protocol.messages.cardverification

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Card Verification response — PDF §5.7.2, message code 'E'. */
sealed interface CardVerificationResponse {
    val terminalId: String
    val resultCode: TransactionResultCode
    val trailer: ResponseTrailer

    data class Approved(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val maskedPan: MaskedPan,
        val transactionType: CardTransactionType?,
        val authorizationCode: String,
        val transactionDateTime: String,
        override val trailer: ResponseTrailer,
    ) : CardVerificationResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        val actionCode: String,
        override val trailer: ResponseTrailer,
    ) : CardVerificationResponse
}
