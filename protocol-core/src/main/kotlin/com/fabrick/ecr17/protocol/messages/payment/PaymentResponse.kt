package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** DCC data present only in the 'V' response variant (§5.1.3) when the currency-exchange flag is '1'. */
data class CurrencyExchangeData(
    val originalAmountMinorUnits: Long,
    val exchangeRate: String,
    val currencyCode: String,
    val transactionAmountInCurrency: Long,
    val precisionDecimals: Int,
)

/** Basic Payment response — PDF §5.1.2 (code 'E', no DCC) / §5.1.3 (code 'V', with DCC). */
sealed interface PaymentResponse {
    val terminalId: String
    val resultCode: TransactionResultCode

    data class Approved(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val maskedPan: MaskedPan,
        val transactionType: CardTransactionType?,
        val authorizationCode: String,
        val transactionDateTime: String,
        val trailer: ResponseTrailer,
        /** Only present when parsed from the 'V' (currency-exchange) response variant, §5.1.3 pos 72. */
        val actionCode: String? = null,
        val currencyExchange: CurrencyExchangeData? = null,
    ) : PaymentResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        val trailer: ResponseTrailer,
        val actionCode: String? = null,
    ) : PaymentResponse
}
