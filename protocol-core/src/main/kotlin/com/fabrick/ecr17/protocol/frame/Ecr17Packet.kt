package com.fabrick.ecr17.protocol.frame

/** A single decoded unit from the ECR17 byte stream. See §4.1-§4.3 of the PDF. */
sealed interface Ecr17Packet {

    /** An application-level packet: STX + [message] (ASCII) + ETX + LRC. Requires a physical ACK/NAK. */
    data class Application(val message: String, val raw: ByteArray) : Ecr17Packet {
        override fun equals(other: Any?): Boolean =
            other is Application && message == other.message && raw.contentEquals(other.raw)

        override fun hashCode(): Int = 31 * message.hashCode() + raw.contentHashCode()
    }

    /** Physical confirmation: ACK + ETX + LRC. */
    data class Ack(val raw: ByteArray) : Ecr17Packet {
        override fun equals(other: Any?): Boolean = other is Ack && raw.contentEquals(other.raw)
        override fun hashCode(): Int = raw.contentHashCode()
    }

    /** Physical refusal: NAK + ETX + LRC. */
    data class Nak(val raw: ByteArray) : Ecr17Packet {
        override fun equals(other: Any?): Boolean = other is Nak && raw.contentEquals(other.raw)
        override fun hashCode(): Int = raw.contentHashCode()
    }

    /** Procedure progress update: SOH + 20-char [message] + EOT. No physical ACK required. */
    data class Progress(val message: String, val raw: ByteArray) : Ecr17Packet {
        override fun equals(other: Any?): Boolean =
            other is Progress && message == other.message && raw.contentEquals(other.raw)

        override fun hashCode(): Int = 31 * message.hashCode() + raw.contentHashCode()
    }

    /** Bytes that could not be interpreted as any known packet type (malformed control byte, bad LRC, etc.). */
    data class Invalid(val reason: String, val raw: ByteArray) : Ecr17Packet {
        override fun equals(other: Any?): Boolean =
            other is Invalid && reason == other.reason && raw.contentEquals(other.raw)

        override fun hashCode(): Int = 31 * reason.hashCode() + raw.contentHashCode()
    }
}
