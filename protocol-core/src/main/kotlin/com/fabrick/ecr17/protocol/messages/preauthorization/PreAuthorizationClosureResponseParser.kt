package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.CardType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/**
 * Parses Pre-authorization closure responses: message code 'c' (§5.17.2, no currency
 * exchange) or 'v' (§5.17.3, with currency exchange).
 */
object PreAuthorizationClosureResponseParser : Ecr17ResponseParser<PreAuthorizationClosureResponse> {

    override fun parse(message: String): PreAuthorizationClosureResponse {
        require(message.length >= 74) { "Pre-authorization closure response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "c" || code == "v") { "Unexpected message code '$code' for Pre-authorization closure response" }
        if (code == "v") {
            require(message.length >= 109) { "Pre-authorization closure (DCC) response too short: ${message.length} chars" }
        }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr) ?: error("Unknown Pre-authorization closure result code '$resultStr'")
        val terminalId = message.protocolField(1, 8)

        val trailer = ResponseTrailer(
            cardType = CardType.fromCode(message.protocolField(48, 1).first()),
            acquirerId = message.protocolField(49, 11).trimEnd(),
            stan = message.protocolField(60, 6),
            idOnline = message.protocolField(66, 6),
        )
        val actionCode = message.protocolField(72, 3)

        return if (resultCode == TransactionResultCode.OK) {
            val currencyExchange = if (code == "v" && message.protocolField(75, 1) == "1") {
                PreAuthorizationClosureCurrencyExchange(
                    exchangeRate = message.protocolField(76, 8),
                    currencyCode = message.protocolField(84, 3),
                    transactionAmountInCurrency = message.protocolField(87, 12).toLong(),
                    precisionDecimals = message.protocolField(99, 1).toInt(),
                )
            } else {
                null
            }
            PreAuthorizationClosureResponse.Approved(
                terminalId = terminalId,
                resultCode = resultCode,
                maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
                transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
                authorizationCode = message.protocolField(35, 6),
                transactionDateTime = message.protocolField(41, 7),
                trailer = trailer,
                actionCode = actionCode,
                currencyExchange = currencyExchange,
            )
        } else {
            PreAuthorizationClosureResponse.Denied(
                terminalId = terminalId,
                resultCode = resultCode,
                resultDescription = message.protocolField(13, 24).trimEnd(),
                trailer = trailer,
                actionCode = actionCode,
            )
        }
    }
}
