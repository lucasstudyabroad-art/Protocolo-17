package com.fabrick.ecr17.protocol.messages.refund

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Refund response — PDF §5.3.2, message code 'A'. Unlike Payment, fields are unconditional (no positive/negative branching). */
data class RefundResponse(
    val terminalId: String,
    val resultCode: TransactionResultCode,
    val maskedPan: MaskedPan,
    val transactionType: CardTransactionType?,
    val authorizationCode: String,
    val acquirerId: String,
    val transactionDateTime: String,
)
