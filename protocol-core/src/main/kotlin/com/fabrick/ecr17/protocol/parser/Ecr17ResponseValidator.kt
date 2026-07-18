package com.fabrick.ecr17.protocol.parser

import com.fabrick.ecr17.protocol.core.Ecr17Constants

/**
 * Validates the Terminal ID embedded in an incoming application message against the
 * configured Terminal ID, per §2.1: "If the function is enabled, the terminal will verify
 * that the terminal ID specified in cash register messages, is equal to the one sent to
 * cash register, or 00000000." The ECR side applies the same rule symmetrically to
 * responses it receives.
 */
object Ecr17ResponseValidator {
    fun terminalIdMatches(message: String, expectedTerminalId: String): Boolean {
        if (message.length < Ecr17Constants.TERMINAL_ID_LENGTH) return false
        val actual = message.take(Ecr17Constants.TERMINAL_ID_LENGTH)
        return actual == expectedTerminalId || actual == Ecr17Constants.TERMINAL_ID_WILDCARD
    }
}
