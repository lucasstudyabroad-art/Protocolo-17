package com.fabrick.ecr17.protocol.messages.receipt

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType

/** §5.12.1 pos 11 values. Only ACK/NAK confirms this message — there is no application-level response. */
enum class ReceiptPrintMode(val code: Char, val description: String) {
    DISABLE_ON_ECR('0', "Disable printing receipt on ECR — terminal prints every receipt"),
    ENABLE_ON_ECR('1', "Enable printing receipt on ECR — every receipt is sent via 'S' command"),
    ENABLE_BOTH('2', "Enable printing on both terminal and ECR"),
    MERCHANT_ON_ECR_CUSTOMER_ON_TERMINAL('3', "Merchant receipt on ECR, customer copy on terminal's printer"),
    ;

    companion object {
        fun fromCode(code: Char): ReceiptPrintMode? = entries.find { it.code == code }
    }
}

/**
 * Enable/disable printing receipt on ECR — PDF §5.12.1, message code 'E'.
 *
 * Note: shares the letter 'E' with the Payment/Reversal/etc. *response* code, but this is
 * always an ECR-originated request whose only reply is the physical ACK/NAK — never an
 * application-level response message.
 */
data class ReceiptModeRequest(
    val terminalId: String,
    val mode: ReceiptPrintMode,
) : Ecr17Request {

    override val operationType = OperationType.RECEIPT_MODE
    override val messageCode = 'E'

    override fun applicationMessage(): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId")) // pos 1-8
        append('0') // pos 9
        append(messageCode) // pos 10
        append(mode.code) // pos 11
    }
}
