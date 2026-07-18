package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Incremental authorization response — PDF §5.16.2, message code 'i'. Fields are unconditional (no positive/negative branching, like Refund). */
data class IncrementalAuthorizationResponse(
    val terminalId: String,
    val resultCode: TransactionResultCode,
    val maskedPan: MaskedPan,
    val transactionType: CardTransactionType?,
    val acquirerId: String,
    val authorizationCode: String,
    val stan: String,
    val idOnline: String,
    val transactionDateTime: String,
    val actionCode: String,
)
