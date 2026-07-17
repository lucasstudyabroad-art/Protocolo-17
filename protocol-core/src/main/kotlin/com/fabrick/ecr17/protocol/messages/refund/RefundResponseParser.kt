package com.fabrick.ecr17.protocol.messages.refund

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.CardTransactionType
import com.fabrick.ecr17.protocol.messages.common.MaskedPan
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Parses Refund responses (§5.3.2, message code 'A'). */
object RefundResponseParser : Ecr17ResponseParser<RefundResponse> {
    override fun parse(message: String): RefundResponse {
        require(message.length >= 58) { "Refund response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "A") { "Unexpected message code '$code' for Refund response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr) ?: error("Unknown Refund transaction result code '$resultStr'")

        return RefundResponse(
            terminalId = message.protocolField(1, 8),
            resultCode = resultCode,
            maskedPan = MaskedPan.fromTerminalField(message.protocolField(13, 19)),
            transactionType = CardTransactionType.fromCode(message.protocolField(32, 3)),
            authorizationCode = message.protocolField(35, 6),
            acquirerId = message.protocolField(41, 11).trimEnd(),
            transactionDateTime = message.protocolField(52, 7),
        )
    }
}
