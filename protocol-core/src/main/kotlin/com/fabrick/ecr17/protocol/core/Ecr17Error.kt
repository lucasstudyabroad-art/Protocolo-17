package com.fabrick.ecr17.protocol.core

/** Typed error taxonomy for every failure mode the ECR17 stack can surface. */
sealed interface Ecr17Error {
    val message: String

    data class Configuration(override val message: String) : Ecr17Error
    data class NetworkUnavailable(override val message: String = "No active network connection") : Ecr17Error
    data class ConnectionTimeout(override val message: String = "Timed out connecting to the POS terminal") : Ecr17Error
    data class ConnectionRefused(override val message: String = "The POS terminal refused the connection") : Ecr17Error
    data class SocketClosed(override val message: String = "Connection to the POS terminal was closed unexpectedly") : Ecr17Error
    data class AckTimeout(override val message: String = "No ACK/NAK received from the POS terminal in time") : Ecr17Error
    data class NakLimitReached(override val message: String = "Maximum retransmissions reached after repeated NAK") : Ecr17Error
    data class ResponseTimeout(override val message: String = "No response received from the POS terminal in time") : Ecr17Error
    data class InvalidFrame(override val message: String) : Ecr17Error
    data class InvalidLrc(override val message: String = "LRC check failed on incoming frame") : Ecr17Error
    data class UnexpectedMessage(override val message: String) : Ecr17Error
    data class TerminalIdMismatch(override val message: String) : Ecr17Error
    data class InvalidResponseLength(override val message: String) : Ecr17Error
    data class TerminalNotOperative(override val message: String) : Ecr17Error
    data class Parsing(override val message: String) : Ecr17Error
    data class Unknown(override val message: String) : Ecr17Error

    companion object {
        /** Maps any error to a short, non-technical message safe to show on the main UI. */
        fun userMessage(error: Ecr17Error): String = when (error) {
            is Configuration -> "Configuration incomplete: ${error.message}"
            is NetworkUnavailable -> "No network connection. Check your Wi-Fi/LAN connection."
            is ConnectionTimeout -> "Could not reach the POS terminal. Verify the IP address and port."
            is ConnectionRefused -> "The POS terminal refused the connection. Verify the port and PAXTools configuration."
            is SocketClosed -> "Connection lost during the operation."
            is AckTimeout -> "The POS terminal did not confirm the request in time."
            is NakLimitReached -> "The POS terminal repeatedly rejected the request."
            is ResponseTimeout -> "The POS terminal did not respond in time."
            is InvalidFrame -> "Received a malformed response from the POS terminal."
            is InvalidLrc -> "Received a corrupted response from the POS terminal."
            is UnexpectedMessage -> "Received an unexpected response from the POS terminal."
            is TerminalIdMismatch -> "The POS terminal ID does not match the configured Terminal ID."
            is InvalidResponseLength -> "Received a response of unexpected length from the POS terminal."
            is TerminalNotOperative -> "The POS terminal is not operative."
            is Parsing -> "Could not interpret the POS terminal's response."
            is Unknown -> "An unexpected error occurred."
        }
    }
}
