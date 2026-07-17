package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.CardType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/**
 * Parses Basic Payment responses: message code 'E' (§5.1.2, no currency exchange) or 'V'
 * (§5.1.3, with currency exchange).
 */
object PaymentResponseParser : Ecr17ResponseParser<PaymentResponse> {

    override fun parse(message: String): PaymentResponse {
        require(message.length >= 71) { "Payment response too short: ${message.length} chars" }
        val terminalId = message.protocolField(1, 8)
        val code = message.protocolField(10, 1)
        require(code == "E" || code == "V") { "Unexpected message code '$code' for Payment response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr)
            ?: error("Unknown Payment transaction result code '$resultStr'")

        return if (code == "V") parseWithDcc(message, terminalId, resultCode) else parseWithoutDcc(message, terminalId, resultCode)
    }

    private fun parseWithoutDcc(message: String, terminalId: String, resultCode: TransactionResultCode): PaymentResponse {
        return if (resultCode == TransactionResultCode.OK) {
            require(message.length >= 71) { "Positive Payment response too short" }
            PaymentResponse.Approved(
                terminalId = terminalId,
                resultCode = resultCode,
                maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
                transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
                authorizationCode = message.protocolField(35, 6),
                transactionDateTime = message.protocolField(41, 7),
                trailer = trailerNoActionCode(message),
            )
        } else {
            PaymentResponse.Denied(
                terminalId = terminalId,
                resultCode = resultCode,
                resultDescription = message.protocolField(13, 24).trimEnd(),
                trailer = trailerNoActionCode(message),
            )
        }
    }

    private fun parseWithDcc(message: String, terminalId: String, resultCode: TransactionResultCode): PaymentResponse {
        require(message.length >= 117) { "Payment (DCC) response too short: ${message.length} chars" }
        val dccFlag = message.protocolField(83, 1)
        val currencyExchange = if (dccFlag == "1") {
            CurrencyExchangeData(
                originalAmountMinorUnits = message.protocolField(75, 8).toLong(),
                exchangeRate = message.protocolField(84, 8),
                currencyCode = message.protocolField(92, 3),
                transactionAmountInCurrency = message.protocolField(95, 12).toLong(),
                precisionDecimals = message.protocolField(107, 1).toInt(),
            )
        } else {
            null
        }

        val actionCode = message.protocolField(72, 3)
        return if (resultCode == TransactionResultCode.OK) {
            PaymentResponse.Approved(
                terminalId = terminalId,
                resultCode = resultCode,
                maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
                transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
                authorizationCode = message.protocolField(35, 6),
                transactionDateTime = message.protocolField(41, 7),
                trailer = trailerNoActionCode(message),
                actionCode = actionCode,
                currencyExchange = currencyExchange,
            )
        } else {
            PaymentResponse.Denied(
                terminalId = terminalId,
                resultCode = resultCode,
                resultDescription = message.protocolField(13, 24).trimEnd(),
                trailer = trailerNoActionCode(message),
                actionCode = actionCode,
            )
        }
    }

    /** §5.1.2/§5.1.3 common trailer: cardType(48) acquirer(49) STAN(60) idOnline(66). */
    private fun trailerNoActionCode(message: String): ResponseTrailer = ResponseTrailer(
        cardType = CardType.fromCode(message.protocolField(48, 1).first()),
        acquirerId = message.protocolField(49, 11).trimEnd(),
        stan = message.protocolField(60, 6),
        idOnline = message.protocolField(66, 6),
    )
}
