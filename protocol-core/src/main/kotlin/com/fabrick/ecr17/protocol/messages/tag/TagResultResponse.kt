package com.fabrick.ecr17.protocol.messages.tag

/** Additional data from GT result response — PDF §5.10.2, message code 'U'. */
data class TagResultResponse(
    val terminalId: String,
    val additionalData: String,
)
