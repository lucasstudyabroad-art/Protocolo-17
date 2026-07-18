package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Parses Incremental authorization responses (§5.16.2, message code 'i'). */
object IncrementalAuthorizationResponseParser : Ecr17ResponseParser<IncrementalAuthorizationResponse> {
    override fun parse(message: String): IncrementalAuthorizationResponse {
        require(message.length >= 75) { "Incremental authorization response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "i") { "Unexpected message code '$code' for Incremental authorization response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr) ?: error("Unknown Incremental authorization result code '$resultStr'")

        return IncrementalAuthorizationResponse(
            terminalId = message.protocolField(1, 8),
            resultCode = resultCode,
            maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
            transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
            acquirerId = message.protocolField(35, 11).trimEnd(),
            authorizationCode = message.protocolField(46, 6),
            stan = message.protocolField(52, 6),
            idOnline = message.protocolField(58, 6),
            transactionDateTime = message.protocolField(64, 7),
            actionCode = message.protocolField(71, 3),
        )
    }
}
