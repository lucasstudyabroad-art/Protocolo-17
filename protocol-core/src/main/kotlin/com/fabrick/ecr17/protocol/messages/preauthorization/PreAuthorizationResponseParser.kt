package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.CardType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Parses Pre-authorization responses (§5.15.2, message code 'e'). */
object PreAuthorizationResponseParser : Ecr17ResponseParser<PreAuthorizationResponse> {
    override fun parse(message: String): PreAuthorizationResponse {
        require(message.length >= 106) { "Pre-authorization response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "e") { "Unexpected message code '$code' for Pre-authorization response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr) ?: error("Unknown Pre-authorization result code '$resultStr'")
        val terminalId = message.protocolField(1, 8)
        val cardType = CardType.fromCode(message.protocolField(71, 1).first())
        val acquirerId = message.protocolField(72, 11).trimEnd()
        val stan = message.protocolField(83, 6)
        val idOnline = message.protocolField(89, 6)

        return if (resultCode == TransactionResultCode.OK) {
            PreAuthorizationResponse.Approved(
                terminalId = terminalId,
                resultCode = resultCode,
                maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
                transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
                authorizationCode = message.protocolField(35, 6),
                preAuthorizedAmountMinorUnits = message.protocolField(41, 8).toLong(),
                preAuthorizationCode = message.protocolField(49, 9),
                actionCode = message.protocolField(58, 3),
                transactionDateTime = message.protocolField(61, 7),
                cardType = cardType,
                acquirerId = acquirerId,
                stan = stan,
                idOnline = idOnline,
            )
        } else {
            PreAuthorizationResponse.Denied(
                terminalId = terminalId,
                resultCode = resultCode,
                resultDescription = message.protocolField(13, 24).trimEnd(),
                actionCode = message.protocolField(37, 3),
                cardType = cardType,
                acquirerId = acquirerId,
                stan = stan,
                idOnline = idOnline,
            )
        }
    }
}
