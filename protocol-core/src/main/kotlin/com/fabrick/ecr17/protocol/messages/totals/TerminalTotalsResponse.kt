package com.fabrick.ecr17.protocol.messages.totals

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode

/** Terminal Totals response — PDF §5.5.2, message code 'T'. */
data class TerminalTotalsResponse(
    val terminalId: String,
    val resultCode: TransactionResultCode,
    val posTotalMinorUnits: Long?,
)
