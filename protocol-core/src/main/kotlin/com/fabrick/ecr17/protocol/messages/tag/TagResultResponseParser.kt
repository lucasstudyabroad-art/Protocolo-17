package com.fabrick.ecr17.protocol.messages.tag

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser

/** Parses Additional data from GT result responses (§5.10.2, message code 'U'). */
object TagResultResponseParser : Ecr17ResponseParser<TagResultResponse> {
    override fun parse(message: String): TagResultResponse {
        require(message.length >= 19) { "TAG result response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "U") { "Unexpected message code '$code' for TAG result response" }

        val dataLength = message.protocolField(17, 3).toInt()
        require(message.length >= 19 + dataLength) { "TAG result response shorter than declared data length $dataLength" }
        val additionalData = if (dataLength > 0) message.protocolField(20, dataLength) else ""

        return TagResultResponse(
            terminalId = message.protocolField(1, 8),
            additionalData = additionalData,
        )
    }
}
