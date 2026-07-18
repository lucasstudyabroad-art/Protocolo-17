package com.fabrick.ecr17.protocol.messages.ticket

/**
 * Concatenates one or more [TicketLineMessage] contents into a single ticket/receipt body,
 * per PDF §5.13: "When multiple S messages are received, the ECR is responsible for
 * concatenating them," terminated by six new-line bytes (0x7D) followed by 0x1B.
 *
 * Control characters used by PAX terminals (§5.13, "USED" column):
 * - 0x7D: new line (CR+LF) and normal-format reset.
 * - 0x7F: start of double-height-bold characters (e.g. the amount line on a successful payment).
 * (0x7E/0x7B/0x7C/0x5E are documented but explicitly marked "NOT USED" by PAX terminals.)
 */
class TicketAccumulator {
    private val parts = mutableListOf<String>()
    private var complete = false

    fun accept(content: String) {
        check(!complete) { "ticket is already complete, cannot accept more content" }
        parts.add(content)
        if (rawConcatenated().endsWith(TERMINATOR)) {
            complete = true
        }
    }

    fun isComplete(): Boolean = complete

    fun partCount(): Int = parts.size

    /** Raw concatenation of every accepted part, control characters included. */
    fun rawConcatenated(): String = parts.joinToString(separator = "")

    /** Human-readable rendering: newline bytes become '\n', bold-start markers are stripped, terminator removed. */
    fun renderPlainText(): String {
        var raw = rawConcatenated()
        if (raw.endsWith(TERMINATOR)) raw = raw.removeSuffix(TERMINATOR)
        return raw.replace(NEW_LINE_CHAR, '\n').replace(BOLD_START_CHAR.toString(), "")
    }

    fun reset() {
        parts.clear()
        complete = false
    }

    companion object {
        const val NEW_LINE_CHAR = '}' // 0x7D
        const val BOLD_START_CHAR = '\u007F' // 0x7F — unrelated to the LRC seed value, which is a different byte-role in a different frame layer.
        val TERMINATOR: String = NEW_LINE_CHAR.toString().repeat(6) + '\u001B'
    }
}
