package com.fabrick.ecr17.protocol.messages.common

/** Transaction result codes shared across every financial response (§5.1-§5.18). */
enum class TransactionResultCode(val code: String) {
    OK("00"),
    KO("01"),
    CARD_NOT_PRESENT("05"),
    UNKNOWN_TAG_FROM_GT("09"),
    /** §5.18 Retroactive Reversal: terminal cannot manage the procedure. */
    UNSUPPORTED_PROCEDURE("91"),
    ;

    companion object {
        fun fromCode(code: String): TransactionResultCode? = entries.find { it.code == code }
        fun isPositive(code: String): Boolean = code == OK.code
    }
}

/** Card type, common trailer field (e.g. §5.1.2 pos 48). */
enum class CardType(val code: Char) {
    BANCOMAT('1'),
    CREDIT('2'),
    OTHER('3'),
    ;

    companion object {
        fun fromCode(code: Char): CardType? = entries.find { it.code == code }
    }
}

/** Card/transaction technology, e.g. §5.1.2 pos 32 (ICC/MAG/MAN/CLM/CLI). */
enum class CardTransactionType(val code: String) {
    ICC("ICC"),
    MAG("MAG"),
    MAN("MAN"),
    CLM("CLM"),
    CLI("CLI"),
    ;

    companion object {
        fun fromCode(code: String): CardTransactionType? = entries.find { it.code == code }
    }
}

/**
 * Card PAN reduced to its safe, persistable form immediately at parse time.
 *
 * The terminal already truncates the PAN per PDF §1 ("PAN – Card data": only the last 4
 * digits are sent in clear, the rest as '0'). This type goes one step further and never
 * retains the raw 19-char field at all — only the last 4 digits are kept, so no code path
 * downstream of the parser can accidentally log or persist a longer fragment.
 */
data class MaskedPan(val last4: String) {
    init {
        require(last4.length <= 4) { "last4 must be at most 4 chars" }
    }

    override fun toString(): String = "**** **** **** $last4"

    companion object {
        /** Builds a [MaskedPan] from the raw 19-char terminal field, discarding everything but the last 4 digits. */
        fun fromTerminalField(rawField: String): MaskedPan {
            val digits = rawField.filter { it.isDigit() }
            val last4 = if (digits.length >= 4) digits.takeLast(4) else digits
            return MaskedPan(last4)
        }
    }
}

/** Payment type selector shared by Payment/Extended Payment/Pre-authorization/Card Verification requests. */
enum class PaymentType(val code: Char) {
    AUTO_RECOGNITION('0'),
    DEBIT_ONLY('1'),
    CREDIT_ONLY('2'),
    OTHER('3'),
    ;

    companion object {
        fun fromCode(code: Char): PaymentType? = entries.find { it.code == code }
    }
}

/** §5.1.1 pos 22 and equivalents: whether the card is already inserted when the operation starts. */
enum class CardPresentMode(val code: Char) {
    NOT_YET_PRESENT('0'),
    ALREADY_PRESENT('1'),
    ;

    companion object {
        fun fromCode(code: Char): CardPresentMode? = entries.find { it.code == code }
    }
}

/** Common trailer shared by most positive financial responses: card type, acquirer, STAN, online id. */
data class ResponseTrailer(
    val cardType: CardType?,
    val acquirerId: String,
    val stan: String,
    val idOnline: String,
)
