package com.fabrick.ecr17.protocol.messages.ticket

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType

enum class TicketType(val code: Char) {
    LAST_FINANCIAL('0'),
    LAST_SERVICE('1'),
}

/**
 * Reprint ticket request — PDF §5.14.1, message code 'R'.
 *
 * Note: the PDF's section header for §5.14.1 reads "Send ticket message (from Terminal)",
 * but the field table it introduces is unambiguously an ECR-originated request (Terminal
 * ID, reserved, message code 'R', print-on-ECR flag, ticket-type flag) — this looks like a
 * copy/paste heading error in the source document. This implementation follows the table
 * (ECR -> Terminal request), consistent with §5.14's prose ("the ECR ... sends the terminal
 * this command"). See `docs/protocol-assumptions.md`.
 *
 * The terminal either prints locally or replies with one or more [TicketLineMessage]s
 * (code 'S'), depending on [printOnEcr].
 */
data class ReprintTicketRequest(
    val terminalId: String,
    val printOnEcr: Boolean = false,
    val ticketType: TicketType = TicketType.LAST_FINANCIAL,
) : Ecr17Request {

    override val operationType = OperationType.REPRINT_TICKET
    override val messageCode = 'R'

    override fun applicationMessage(): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId")) // pos 1-8
        append('0') // pos 9, reserved
        append(messageCode) // pos 10
        append(if (printOnEcr) '1' else '0') // pos 11
        append(ticketType.code) // pos 12
        append("0".repeat(10)) // pos 13-22, reserved
    }
}
