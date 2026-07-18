package com.fabrick.ecr17.protocol.transport

/** Result of one [PosSocketTransport.receive] call. */
sealed interface TransportEvent {
    data class Data(val bytes: ByteArray) : TransportEvent {
        override fun equals(other: Any?): Boolean = other is Data && bytes.contentEquals(other.bytes)
        override fun hashCode(): Int = bytes.contentHashCode()
    }

    /** No bytes arrived within the requested timeout; the connection is still considered open. */
    data object Timeout : TransportEvent

    /** The peer closed the connection (EOF) or the socket errored out. */
    data object Closed : TransportEvent
}

/**
 * Raw TCP transport abstraction, deliberately kept separate from the ECR17 codec so the
 * connection direction/implementation can change without touching protocol logic (the
 * task's default is "Android app connects as TCP client to the POS," but real deployments
 * may require the reverse).
 */
interface PosSocketTransport {
    val isConnected: Boolean

    suspend fun connect(host: String, port: Int, connectTimeoutMs: Int)

    suspend fun send(bytes: ByteArray)

    /** Waits up to [timeoutMs] for at least one byte to arrive. */
    suspend fun receive(timeoutMs: Int): TransportEvent

    suspend fun close()
}
