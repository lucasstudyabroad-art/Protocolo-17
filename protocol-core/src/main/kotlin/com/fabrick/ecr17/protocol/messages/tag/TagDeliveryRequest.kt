package com.fabrick.ecr17.protocol.messages.tag

import com.fabrick.ecr17.protocol.core.Ecr17Field
import com.fabrick.ecr17.protocol.core.Ecr17Request
import com.fabrick.ecr17.protocol.core.OperationType

/** §5.10.1 pos 11: payment type this additional-data message relates to. */
enum class TagPaymentType(val code: Char) {
    STANDARD_PAYMENT('0'),
    BILL_PAYMENT('1'),
}

/**
 * Additional data for GT delivery request — PDF §5.10.1, message code 'U'.
 *
 * The PDF's own text says the "Payment Type" value is a single digit ('0' or '1') but the
 * field table gives it length 6 — an internal inconsistency in the source document. This
 * implementation follows the table literally (6-char zero-padded numeric field) per the
 * "PDF is authoritative, document deviations" rule; see `docs/protocol-assumptions.md`.
 *
 * [exclusiveTagIndex] is a 4-char byte-map field whose characters may be digits '0'-'9' or
 * '*' (§5.10.1 pos 28) — not purely numeric, so it is validated and stored as a raw string.
 *
 * [tagContents] holds 0-4 "privative TAG content" entries (§5.10.1 pos 37), each 1-100
 * ASCII characters; every entry is terminated on the wire by an end-of-field byte (0x1B).
 */
data class TagDeliveryRequest(
    val terminalId: String,
    val paymentType: TagPaymentType = TagPaymentType.STANDARD_PAYMENT,
    /** "00" = no data expected back from GT. Currently fixed at "62" per the PDF's field note. */
    val isoField: String = "62",
    /** Currently fixed at "DF8D01" per the PDF's field note. */
    val tagNumber: String = "DF8D01",
    val exclusiveTagIndex: String = "0000",
    val tagContents: List<String> = emptyList(),
) : Ecr17Request {

    override val operationType = OperationType.TAG_DELIVERY
    override val messageCode = 'U'

    init {
        require(tagContents.size <= MAX_TAG_ENTRIES) { "at most $MAX_TAG_ENTRIES TAG content entries allowed, got ${tagContents.size}" }
        tagContents.forEach { entry ->
            require(entry.length in 1..100) { "TAG content entry must be 1-100 chars, was ${entry.length}" }
            Ecr17Field.requireAscii(entry, "tagContent")
        }
        require(exclusiveTagIndex.length == 4) { "exclusiveTagIndex must be exactly 4 chars" }
        require(exclusiveTagIndex.all { it in '0'..'9' || it == '*' }) { "exclusiveTagIndex chars must be digits or '*'" }
    }

    override fun applicationMessage(): String = buildString {
        append(Ecr17Field.fixedNumeric(terminalId, 8, "terminalId")) // pos 1-8
        append('0') // pos 9
        append(messageCode) // pos 10
        append(Ecr17Field.fixedNumeric(paymentType.code.toString(), 6, "paymentType")) // pos 11-16
        append(Ecr17Field.fixedNumeric(isoField, 2, "isoField")) // pos 17-18
        append(Ecr17Field.fixedAlphanumeric(tagNumber, 8, leftAlign = true)) // pos 19-26
        append('0') // pos 27, reserved
        append(exclusiveTagIndex.padEnd(4, '0')) // pos 28-31
        append("00000") // pos 32-36, reserved
        tagContents.forEach { entry ->
            append(entry)
            append(END_OF_FIELD)
        }
    }

    companion object {
        const val MAX_TAG_ENTRIES = 4
        const val END_OF_FIELD: Char = '\u001B'
    }
}
