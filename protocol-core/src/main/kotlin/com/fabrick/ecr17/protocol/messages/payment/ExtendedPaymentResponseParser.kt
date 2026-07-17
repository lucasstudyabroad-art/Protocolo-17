package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.CardType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Parses Payment-with-extended-result responses (§5.2.2, message code 'E'). */
object ExtendedPaymentResponseParser : Ecr17ResponseParser<ExtendedPaymentResponse> {

    override fun parse(message: String): ExtendedPaymentResponse {
        require(message.length >= 92) { "Extended Payment response too short: ${message.length} chars" }
        val terminalId = message.protocolField(1, 8)
        val code = message.protocolField(10, 1)
        require(code == "E") { "Unexpected message code '$code' for Extended Payment response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr)
            ?: error("Unknown Extended Payment transaction result code '$resultStr'")

        val trailer = ResponseTrailer(
            cardType = CardType.fromCode(message.protocolField(48, 1).first()),
            acquirerId = message.protocolField(49, 11).trimEnd(),
            stan = message.protocolField(60, 6),
            idOnline = message.protocolField(66, 6),
        )
        val actionCode = message.protocolField(72, 3)
        val amountReceived = message.protocolField(75, 8).toLong()

        return if (resultCode == TransactionResultCode.OK) {
            ExtendedPaymentResponse.Approved(
                terminalId = terminalId,
                resultCode = resultCode,
                maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
                transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
                authorizationCode = message.protocolField(35, 6),
                transactionDateTime = message.protocolField(41, 7),
                trailer = trailer,
                actionCode = actionCode,
                amountReceivedByHostMinorUnits = amountReceived,
            )
        } else {
            ExtendedPaymentResponse.Denied(
                terminalId = terminalId,
                resultCode = resultCode,
                resultDescription = message.protocolField(13, 24).trimEnd(),
                trailer = trailer,
                actionCode = actionCode,
                amountReceivedByHostMinorUnits = amountReceived,
            )
        }
    }
}
