package com.fabrick.ecr17.protocol.messages.ticket

import com.fabrick.ecr17.protocol.core.Ecr17Field.protocolField
import com.fabrick.ecr17.protocol.core.Ecr17ResponseParser

/** A single Send Ticket message — PDF §5.13.1, message code 'S' (Terminal -> ECR), 1-200 char content. */
data class TicketLineMessage(
    val terminalId: String,
    val content: String,
)

/** Parses a single Send Ticket frame. Each frame must be individually ACKed by the ECR (§5.13). */
object TicketLineMessageParser : Ecr17ResponseParser<TicketLineMessage> {
    override fun parse(message: String): TicketLineMessage {
        require(message.length >= 11) { "Send Ticket message too short: ${message.length} chars" }
        val code = message.protocolField(10, 1)
        require(code == "S") { "Unexpected message code '$code' for Send Ticket message" }
        return TicketLineMessage(
            terminalId = message.protocolField(1, 8),
            content = message.substring(10),
        )
    }
}
