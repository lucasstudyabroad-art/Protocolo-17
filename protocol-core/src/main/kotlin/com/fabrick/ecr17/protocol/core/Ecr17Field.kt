package com.fabrick.ecr17.protocol.core

/**
 * Field-level utilities for building and parsing fixed-length ASCII ECR17 fields.
 *
 * All protocol positions in the PDF are 1-based; every function here that accepts a
 * "position" argument follows that convention and converts to a 0-based index
 * internally, so call sites can copy the PDF's "Pos" column value verbatim.
 */
object Ecr17Field {

    /** Throws if [value] contains any byte outside the 0..127 ASCII range (PDF §4.1: "all data ... is ASCII (0 to 127)"). */
    fun requireAscii(value: String, fieldName: String = "field"): String {
        for (ch in value) {
            require(ch.code in 0..127) { "$fieldName contains a non-ASCII character: '$ch' (0x${ch.code.toString(16)})" }
        }
        return value
    }

    /** Throws unless every character of [value] is an ASCII digit 0-9. */
    fun requireNumeric(value: String, fieldName: String = "field"): String {
        require(value.isNotEmpty()) { "$fieldName must not be empty" }
        require(value.all { it in '0'..'9' }) { "$fieldName must be numeric, was '$value'" }
        return value
    }

    /** Builds a numeric field of exactly [length] chars, right-aligned, left-padded with '0'. */
    fun fixedNumeric(value: Long, length: Int, fieldName: String = "field"): String {
        require(value >= 0) { "$fieldName must not be negative, was $value" }
        val raw = value.toString()
        require(raw.length <= length) { "$fieldName value $value does not fit in $length digits" }
        return padLeftZeros(raw, length)
    }

    /** Builds a numeric field from a pre-formatted numeric string, validating length and digits. */
    fun fixedNumeric(value: String, length: Int, fieldName: String = "field"): String {
        requireNumeric(value, fieldName)
        require(value.length <= length) { "$fieldName value '$value' does not fit in $length digits" }
        return padLeftZeros(value, length)
    }

    /** Builds an alphanumeric field of exactly [length] chars. [leftAlign] true pads on the right, false pads on the left. */
    fun fixedAlphanumeric(
        value: String,
        length: Int,
        padChar: Char = ' ',
        leftAlign: Boolean = true,
        fieldName: String = "field",
    ): String {
        requireAscii(value, fieldName)
        require(value.length <= length) { "$fieldName value '$value' (${value.length} chars) does not fit in $length chars" }
        return if (leftAlign) padRightSpaces(value, length, padChar) else padLeftSpaces(value, length, padChar)
    }

    fun padLeftZeros(value: String, length: Int): String {
        require(value.length <= length) { "value '$value' longer than target length $length" }
        return value.padStart(length, '0')
    }

    fun padLeftSpaces(value: String, length: Int, padChar: Char = ' '): String {
        require(value.length <= length) { "value '$value' longer than target length $length" }
        return value.padStart(length, padChar)
    }

    fun padRightSpaces(value: String, length: Int, padChar: Char = ' '): String {
        require(value.length <= length) { "value '$value' longer than target length $length" }
        return value.padEnd(length, padChar)
    }

    /** Validates that [value] is exactly [length] characters. */
    fun validateExactLength(value: String, length: Int, fieldName: String = "field") {
        require(value.length == length) { "$fieldName must be exactly $length chars, was ${value.length} ('$value')" }
    }

    /**
     * Extracts the field at the PDF's 1-based [position] with the given [length].
     * Boundaries are validated before the substring operation to avoid StringIndexOutOfBounds.
     */
    fun String.protocolField(position: Int, length: Int): String {
        val startIndex = position - 1
        require(startIndex >= 0) { "position must be >= 1, was $position" }
        val endIndex = startIndex + length
        require(endIndex <= this.length) {
            "cannot read $length chars at position $position: message is only ${this.length} chars long"
        }
        return substring(startIndex, endIndex)
    }
}
