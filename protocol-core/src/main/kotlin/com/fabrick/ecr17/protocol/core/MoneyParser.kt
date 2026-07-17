package com.fabrick.ecr17.protocol.core

/** Result of parsing user-entered amount text. */
sealed interface MoneyParseResult {
    data class Valid(val minorUnits: Long) : MoneyParseResult
    data class Invalid(val reason: String) : MoneyParseResult
}

/**
 * Parses/formats monetary amounts as Long minor units (cents). Never uses Float/Double.
 *
 * Accepts "10", "10.50", "10,50" (both '.' and ',' as decimal separator, at most one of
 * either), rejects negative values, exponent notation, alphabetic characters, and more
 * than two decimal digits. The protocol's amount field (§5.1.1 pos 24, length 8) can
 * hold at most 8 digits, i.e. a maximum of 99999999 cents = 999999.99.
 */
object MoneyParser {

    const val PROTOCOL_AMOUNT_FIELD_LENGTH = 8
    const val MAX_MINOR_UNITS = 99_999_999L

    private val ALLOWED_CHARS = ('0'..'9').toSet() + setOf('.', ',')

    fun parse(input: String): MoneyParseResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return MoneyParseResult.Invalid("Amount is empty")
        if (trimmed.any { it !in ALLOWED_CHARS }) {
            return MoneyParseResult.Invalid("Amount contains invalid characters")
        }
        if (trimmed.startsWith("-")) return MoneyParseResult.Invalid("Amount must not be negative")

        val separators = trimmed.count { it == '.' || it == ',' }
        if (separators > 1) return MoneyParseResult.Invalid("Amount has more than one decimal separator")

        val normalized = trimmed.replace(',', '.')
        val parts = normalized.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else ""

        if (integerPart.isEmpty() && decimalPart.isEmpty()) {
            return MoneyParseResult.Invalid("Amount is empty")
        }
        if (decimalPart.length > 2) {
            return MoneyParseResult.Invalid("Amount must have at most two decimal places")
        }
        if (integerPart.isNotEmpty() && integerPart.any { it !in '0'..'9' }) {
            return MoneyParseResult.Invalid("Amount has an invalid integer part")
        }
        if (decimalPart.isNotEmpty() && decimalPart.any { it !in '0'..'9' }) {
            return MoneyParseResult.Invalid("Amount has an invalid decimal part")
        }

        val wholeUnits = if (integerPart.isEmpty()) 0L else integerPart.toLongOrNull()
            ?: return MoneyParseResult.Invalid("Amount is too large")
        val paddedDecimal = decimalPart.padEnd(2, '0')
        val minorFraction = if (paddedDecimal.isEmpty()) 0L else paddedDecimal.toLong()

        val minorUnits = try {
            Math.addExact(Math.multiplyExact(wholeUnits, 100L), minorFraction)
        } catch (e: ArithmeticException) {
            return MoneyParseResult.Invalid("Amount is too large")
        }

        if (minorUnits <= 0L) return MoneyParseResult.Invalid("Amount must be greater than zero")
        if (minorUnits > MAX_MINOR_UNITS) {
            return MoneyParseResult.Invalid("Amount exceeds the maximum supported by the protocol (999999.99)")
        }
        return MoneyParseResult.Valid(minorUnits)
    }

    /** Formats minor units into the fixed 8-char, zero-padded protocol amount field. */
    fun toProtocolField(minorUnits: Long): String {
        require(minorUnits in 0..MAX_MINOR_UNITS) { "amount $minorUnits out of protocol range" }
        return Ecr17Field.fixedNumeric(minorUnits, PROTOCOL_AMOUNT_FIELD_LENGTH)
    }

    /** Parses a fixed protocol amount field back into minor units. */
    fun fromProtocolField(field: String): Long {
        Ecr17Field.requireNumeric(field, "amount field")
        return field.toLong()
    }

    /** Formats minor units for display with the given currency symbol, e.g. 650L, "€" -> "€ 6,50". */
    fun formatForDisplay(minorUnits: Long, currencySymbol: String): String {
        val whole = minorUnits / 100
        val fraction = minorUnits % 100
        return "$currencySymbol %d,%02d".format(whole, fraction)
    }
}
