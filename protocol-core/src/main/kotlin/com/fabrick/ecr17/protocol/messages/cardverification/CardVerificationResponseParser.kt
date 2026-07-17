package com.fabrick.ecr17.protocol.messages.cardverification

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.CardType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.ResponseTrailer
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Parses Card Verification responses (§5.7.2, message code 'E'). */
object CardVerificationResponseParser : Ecr17ResponseParser<CardVerificationResponse> {
    override fun parse(message: String): CardVerificationResponse {
        require(message.length >= 71) { "Card Verification response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "E") { "Unexpected message code '$code' for Card Verification response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr) ?: error("Unknown Card Verification result code '$resultStr'")
        val terminalId = message.protocolField(1, 8)

        val trailer = ResponseTrailer(
            cardType = CardType.fromCode(message.protocolField(48, 1).first()),
            acquirerId = message.protocolField(49, 11).trimEnd(),
            stan = message.protocolField(60, 6),
            idOnline = message.protocolField(66, 6),
        )

        return if (resultCode == TransactionResultCode.OK) {
            CardVerificationResponse.Approved(
                terminalId = terminalId,
                resultCode = resultCode,
                maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
                transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
                authorizationCode = message.protocolField(35, 6),
                transactionDateTime = message.protocolField(41, 7),
                trailer = trailer,
            )
        } else {
            CardVerificationResponse.Denied(
                terminalId = terminalId,
                resultCode = resultCode,
                resultDescription = message.protocolField(13, 24).trimEnd(),
                actionCode = message.protocolField(37, 3),
                trailer = trailer,
            )
        }
    }
}
