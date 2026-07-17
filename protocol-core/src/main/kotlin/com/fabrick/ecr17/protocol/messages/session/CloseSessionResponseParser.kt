package com.fabrick.ecr17.protocol.messages.session

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Parses Close Session responses (§5.4.2, message code 'C'). */
object CloseSessionResponseParser : Ecr17ResponseParser<CloseSessionResponse> {
    override fun parse(message: String): CloseSessionResponse {
        require(message.length >= 44) { "Close Session response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "C") { "Unexpected message code '$code' for Close Session response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr) ?: error("Unknown Close Session result code '$resultStr'")
        val terminalId = message.protocolField(1, 8)

        return if (resultCode == TransactionResultCode.OK) {
            CloseSessionResponse.Approved(
                terminalId = terminalId,
                resultCode = resultCode,
                posTotalMinorUnits = message.protocolField(13, 16).toLong(),
                hostTotalMinorUnits = message.protocolField(29, 16).toLong(),
            )
        } else {
            CloseSessionResponse.Denied(
                terminalId = terminalId,
                resultCode = resultCode,
                resultDescription = message.protocolField(13, 19).trimEnd(),
                actionCode = message.protocolField(32, 3),
            )
        }
    }
}
