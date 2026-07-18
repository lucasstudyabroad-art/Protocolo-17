package com.fabrick.ecr17.protocol.messages.lastresult

import com.fabrick.ecr17.protocol.core.OperationType
import com.fabrick.ecr17.protocol.messages.cardverification.CardVerificationResponseParser
import com.fabrick.ecr17.protocol.messages.payment.ExtendedPaymentResponseParser
import com.fabrick.ecr17.protocol.messages.payment.PaymentResponseParser
import com.fabrick.ecr17.protocol.messages.preauthorization.IncrementalAuthorizationResponseParser
import com.fabrick.ecr17.protocol.messages.preauthorization.PreAuthorizationClosureResponseParser
import com.fabrick.ecr17.protocol.messages.preauthorization.PreAuthorizationResponseParser
import com.fabrick.ecr17.protocol.messages.refund.RefundResponseParser
import com.fabrick.ecr17.protocol.messages.reversal.ReversalResponseParser

/**
 * Decodes the "Last Result" replay (§5.11.2): the terminal resends an exact copy of
 * whatever result message it last saved. Because several operations share the same
 * message code (e.g. 'E' is used by Payment, Reversal and Card Verification responses),
 * the wire bytes alone are not enough to disambiguate — the caller must supply
 * [expectedOperation], normally sourced from the last operation recorded in local history.
 *
 * If [expectedOperation] is null or unrecognized, [parse] returns null and the caller can
 * fall back to displaying the raw message for diagnostics rather than guessing its shape.
 */
object Ecr17ResponseDispatcher {
    fun parse(message: String, expectedOperation: OperationType?): Any? = when (expectedOperation) {
        OperationType.PAYMENT -> PaymentResponseParser.parse(message)
        OperationType.EXTENDED_PAYMENT -> ExtendedPaymentResponseParser.parse(message)
        OperationType.REFUND -> RefundResponseParser.parse(message)
        OperationType.REVERSAL -> ReversalResponseParser.parse(message)
        OperationType.CARD_VERIFICATION -> CardVerificationResponseParser.parse(message)
        OperationType.PRE_AUTHORIZATION -> PreAuthorizationResponseParser.parse(message)
        OperationType.INCREMENTAL_AUTHORIZATION -> IncrementalAuthorizationResponseParser.parse(message)
        OperationType.PRE_AUTHORIZATION_CLOSURE -> PreAuthorizationClosureResponseParser.parse(message)
        else -> null
    }
}
