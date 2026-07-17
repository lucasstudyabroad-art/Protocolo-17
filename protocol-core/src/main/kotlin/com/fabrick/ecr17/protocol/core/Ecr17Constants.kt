package com.fabrick.ecr17.protocol.core

/**
 * Control bytes and protocol-wide constants from ECR17 Protocol Specification §2, §4.
 */
object Ecr17Constants {
    const val STX: Byte = 0x02
    const val ETX: Byte = 0x03
    const val SOH: Byte = 0x01
    const val EOT: Byte = 0x04
    const val ACK: Byte = 0x06
    const val NAK: Byte = 0x15

    /** §4.2 — procedure progress update message body length, in bytes. */
    const val PROGRESS_MESSAGE_LENGTH = 20

    /** §2.1 — "Both the terminal and the cash register repeat the message transmission for maximum 3 times". */
    const val PROTOCOL_MAX_TRANSMISSIONS = 3

    /** §2.1 — LRC base/seed value the exclusive-OR chain starts from. */
    const val LRC_INITIAL_VALUE: Byte = 0x7F

    /** §1 — the "J" command must never be used by a new cash command; terminal/ECR must NAK it. */
    const val FORBIDDEN_MESSAGE_CODE = 'J'

    /** §5.1.1 / most requests — Terminal ID field length. */
    const val TERMINAL_ID_LENGTH = 8

    /** §5.1.1 — Cash Register ID field length. */
    const val CASH_REGISTER_ID_LENGTH = 8

    /** Terminal ID value meaning "don't check" per §2.1. */
    const val TERMINAL_ID_WILDCARD = "00000000"
}
