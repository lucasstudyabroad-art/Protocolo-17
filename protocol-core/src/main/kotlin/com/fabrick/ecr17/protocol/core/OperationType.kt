package com.fabrick.ecr17.protocol.core

/** Every ECR17 operation family covered by `docs/ecr17-coverage-matrix.md`. */
enum class OperationType {
    PAYMENT,
    EXTENDED_PAYMENT,
    REFUND,
    CLOSE_SESSION,
    TERMINAL_TOTALS,
    REVERSAL,
    CARD_VERIFICATION,
    RUN_DLL,
    POS_STATUS,
    TAG_DELIVERY,
    LAST_RESULT,
    RECEIPT_MODE,
    REPRINT_TICKET,
    PRE_AUTHORIZATION,
    INCREMENTAL_AUTHORIZATION,
    PRE_AUTHORIZATION_CLOSURE,
    RETROACTIVE_REVERSAL,
}

/** Contract every ECR-originated request builder implements (from the task prompt). */
interface Ecr17Request {
    val operationType: OperationType
    val messageCode: Char
    fun applicationMessage(): String
}

/** Contract every terminal-originated response parser implements. */
interface Ecr17ResponseParser<T> {
    fun parse(message: String): T
}
