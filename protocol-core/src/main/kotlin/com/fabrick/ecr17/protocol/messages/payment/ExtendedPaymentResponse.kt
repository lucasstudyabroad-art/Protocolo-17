package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Payment with extended result response — PDF §5.2.2, message code 'E'. Distinct from [PaymentResponse]: no DCC variant, but carries action code + amount received by host. */
sealed interface ExtendedPaymentResponse {
    val terminalId: String
    val resultCode: TransactionResultCode
    val trailer: ResponseTrailer
    val actionCode: String
    val amountReceivedByHostMinorUnits: Long

    data class Approved(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val maskedPan: MaskedPan,
        val transactionType: CardTransactionType?,
        val authorizationCode: String,
        val transactionDateTime: String,
        override val trailer: ResponseTrailer,
        override val actionCode: String,
        override val amountReceivedByHostMinorUnits: Long,
    ) : ExtendedPaymentResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        override val trailer: ResponseTrailer,
        override val actionCode: String,
        override val amountReceivedByHostMinorUnits: Long,
    ) : ExtendedPaymentResponse
}
