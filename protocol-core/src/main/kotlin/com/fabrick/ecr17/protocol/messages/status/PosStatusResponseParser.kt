package com.fabrick.ecr17.protocol.messages.status

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser

/**
 * Parses POS status responses (§5.9.2, message code 's').
 *
 * Special rule from the PDF: "If the terminal ECR connection parameters are not
 * configured, the command will not have response." This parser only handles the case
 * where a response *was* received; the no-response case must be handled by the transport/
 * session layer as a response timeout, not as a parse error.
 */
object PosStatusResponseParser : Ecr17ResponseParser<PosStatusResponse> {
    override fun parse(message: String): PosStatusResponse {
        require(message.length >= 31) { "POS status response too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "s") { "Unexpected message code '$code' for POS status response" }

        val statusChar = message.protocolField(31, 1).first()
        val status = PosTerminalStatus.fromCode(statusChar)
            ?: error("Unknown POS terminal status code '$statusChar'")

        val swReleaseRaw = if (message.length > 31) message.substring(31) else ""
        val modules = swReleaseRaw.chunked(8)
            .filter { it.length == 8 }
            .map { block -> SoftwareModule(moduleId = block.take(3), versionLabel = block.drop(3)) }

        return PosStatusResponse(
            terminalId = message.protocolField(1, 8),
            terminalDateTimeRaw = message.protocolField(21, 10),
            status = status,
            softwareModules = modules,
        )
    }
}
