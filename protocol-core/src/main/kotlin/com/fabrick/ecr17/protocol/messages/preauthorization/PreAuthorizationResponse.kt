package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.CardType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Pre-authorization response — PDF §5.15.2, message code 'e' (lower case). */
sealed interface PreAuthorizationResponse {
    val terminalId: String
    val resultCode: TransactionResultCode
    val cardType: CardType?
    val acquirerId: String
    val stan: String
    val idOnline: String

    data class Approved(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val maskedPan: MaskedPan,
        val transactionType: CardTransactionType?,
        val authorizationCode: String,
        val preAuthorizedAmountMinorUnits: Long,
        /** Unique pre-authorization identifier printed on the terminal's receipt — needed for Incremental Authorization / Pre-authorization Closure. */
        val preAuthorizationCode: String,
        val actionCode: String,
        val transactionDateTime: String,
        override val cardType: CardType?,
        override val acquirerId: String,
        override val stan: String,
        override val idOnline: String,
    ) : PreAuthorizationResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        val actionCode: String,
        override val cardType: CardType?,
        override val acquirerId: String,
        override val stan: String,
        override val idOnline: String,
    ) : PreAuthorizationResponse
}
