package com.fabrick.ecr17.protocol.messages.dll

/** Run DLL response — PDF §5.8.2, message code 'E'. Only "00"/"01" results are defined (no "09"). */
sealed interface RunDllResponse {
    val terminalId: String
    val stan: String
    val idOnline: String

    data class Approved(
        override val terminalId: String,
        override val stan: String,
        override val idOnline: String,
        val transactionDateTime: String,
    ) : RunDllResponse

    data class Denied(
        override val terminalId: String,
        override val stan: String,
        override val idOnline: String,
        val resultDescription: String,
    ) : RunDllResponse
}
