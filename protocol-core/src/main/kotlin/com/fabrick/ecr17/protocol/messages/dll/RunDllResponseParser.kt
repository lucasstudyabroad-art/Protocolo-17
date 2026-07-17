package com.fabrick.ecr17.protocol.messages.dll

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser

/** Parses Run DLL responses (§5.8.2, message code 'E'). */
object RunDllResponseParser : Ecr17ResponseParser<RunDllResponse> {
    override fun parse(message: String): RunDllResponse {
        require(message.length >= 59) { "Run DLL response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "E") { "Unexpected message code '$code' for Run DLL response" }

        val result = message.protocolField(11, 2)
        require(result == "00" || result == "01") { "Unknown Run DLL result code '$result'" }
        val terminalId = message.protocolField(1, 8)
        val stan = message.protocolField(13, 6)
        val idOnline = message.protocolField(19, 6)

        return if (result == "00") {
            RunDllResponse.Approved(
                terminalId = terminalId,
                stan = stan,
                idOnline = idOnline,
                transactionDateTime = message.protocolField(25, 7),
            )
        } else {
            RunDllResponse.Denied(
                terminalId = terminalId,
                stan = stan,
                idOnline = idOnline,
                resultDescription = message.protocolField(25, 24).trimEnd(),
            )
        }
    }
}
