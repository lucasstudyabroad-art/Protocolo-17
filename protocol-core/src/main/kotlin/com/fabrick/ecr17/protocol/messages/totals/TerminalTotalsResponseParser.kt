package com.fabrick.ecr17.protocol.messages.totals

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Parses Terminal Totals responses (§5.5.2, message code 'T'). */
object TerminalTotalsResponseParser : Ecr17ResponseParser<TerminalTotalsResponse> {
    override fun parse(message: String): TerminalTotalsResponse {
        require(message.length >= 34) { "Terminal Totals response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "T") { "Unexpected message code '$code' for Terminal Totals response" }

        val resultStr = message.protocolField(11, 2)
        val resultCode = TransactionResultCode.fromCode(resultStr) ?: error("Unknown Terminal Totals result code '$resultStr'")

        return TerminalTotalsResponse(
            terminalId = message.protocolField(1, 8),
            resultCode = resultCode,
            posTotalMinorUnits = if (resultCode == TransactionResultCode.OK) message.protocolField(13, 16).toLong() else null,
        )
    }
}
