package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** DCC data present only in the 'v' response variant (§5.17.3) when the currency-exchange flag is '1'. */
data class PreAuthorizationClosureCurrencyExchange(
    val exchangeRate: String,
    val currencyCode: String,
    val transactionAmountInCurrency: Long,
    val precisionDecimals: Int,
)

/** Pre-authorization closure response — PDF §5.17.2 (code 'c', no DCC) / §5.17.3 (code 'v', with DCC). */
sealed interface PreAuthorizationClosureResponse {
    val terminalId: String
    val resultCode: TransactionResultCode
    val trailer: ResponseTrailer
    val actionCode: String

    data class Approved(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val maskedPan: MaskedPan,
        val transactionType: CardTransactionType?,
        val authorizationCode: String,
        val transactionDateTime: String,
        override val trailer: ResponseTrailer,
        override val actionCode: String,
        val currencyExchange: PreAuthorizationClosureCurrencyExchange? = null,
    ) : PreAuthorizationClosureResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        override val trailer: ResponseTrailer,
        override val actionCode: String,
    ) : PreAuthorizationClosureResponse
}
