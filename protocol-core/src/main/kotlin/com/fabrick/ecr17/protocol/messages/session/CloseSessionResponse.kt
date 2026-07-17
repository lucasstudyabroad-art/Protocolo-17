package com.fabrick.ecr17.protocol.messages.session

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Close Session response — PDF §5.4.2, message code 'C'. */
sealed interface CloseSessionResponse {
    val terminalId: String
    val resultCode: TransactionResultCode

    data class Approved(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val posTotalMinorUnits: Long,
        val hostTotalMinorUnits: Long,
    ) : CloseSessionResponse

    data class Denied(
        override val terminalId: String,
        override val resultCode: TransactionResultCode,
        val resultDescription: String,
        val actionCode: String,
    ) : CloseSessionResponse
}
