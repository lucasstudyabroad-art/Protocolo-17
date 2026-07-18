package com.fabrick.ecr17.protocol.service

import com.fabrick.ecr17.protocol.core.Ecr17Constants
import com.fabrick.ecr17.protocol.core.Ecr17Error
import com.fabrick.ecr17.protocol.core.Ecr17OperationResult
import com.fabrick.ecr17.protocol.frame.Ecr17FrameCodec
import com.fabrick.ecr17.protocol.frame.Ecr17Packet
import com.fabrick.ecr17.protocol.parser.Ecr17StreamParser
import com.fabrick.ecr17.protocol.transport.PosSocketTransport
import com.fabrick.ecr17.protocol.transport.TransportEvent

/** One progress update received between ACK and the final response. */
data class ProgressUpdate(val message: String)

/** A single send attempt, recorded for history/diagnostics. */
data class TransmissionAttempt(val attemptNumber: Int, val outcome: String)

/** Everything the caller needs to persist about one request/response exchange. */
data class TransmissionOutcome(
    val result: Ecr17OperationResult<String>,
    val attempts: List<TransmissionAttempt>,
    val progressUpdates: List<ProgressUpdate>,
    val ackReceivedAt: Long?,
)

/**
 * Drives one ECR-originated application exchange over an already-connected
 * [PosSocketTransport]: send, wait for ACK/NAK (retransmitting the *same* frame bytes on
 * NAK or ACK timeout, per §2.1, up to [maxTransmissions]), then wait for zero or more
 * progress packets (§4.2, no physical ACK needed) followed by the final application
 * response, which this session ACKs (or NAKs, on a framing/LRC error) itself.
 *
 * Financial-safety rule (task requirement): once a valid ACK has been received for the
 * request, this session will **never** re-send the request again. If the connection is
 * lost or the final response never arrives after that point, the outcome is
 * [Ecr17OperationResult.Uncertain] — never a silent retry, never [Ecr17OperationResult.Success].
 */
class Ecr17TransmissionSession(
    private val transport: PosSocketTransport,
    private val frameCodec: Ecr17FrameCodec = Ecr17FrameCodec(),
    private val maxTransmissions: Int = Ecr17Constants.PROTOCOL_MAX_TRANSMISSIONS,
) {
    private val parser = Ecr17StreamParser()

    suspend fun execute(
        operationId: String,
        applicationMessage: String,
        ackTimeoutMs: Int,
        responseTimeoutMs: Int,
        progressTimeoutMs: Int,
        onProgress: suspend (ProgressUpdate) -> Unit = {},
    ): TransmissionOutcome {
        parser.reset()
        val frame = frameCodec.encodeApplication(applicationMessage)
        val attempts = mutableListOf<TransmissionAttempt>()
        val progressUpdates = mutableListOf<ProgressUpdate>()

        var attemptNumber = 0
        var ackOutcome: AckOutcome = AckOutcome.NotAcked
        while (attemptNumber < maxTransmissions) {
            attemptNumber++
            try {
                transport.send(frame)
            } catch (e: Exception) {
                attempts.add(TransmissionAttempt(attemptNumber, "send failed: ${e.message}"))
                return TransmissionOutcome(
                    Ecr17OperationResult.Failure(Ecr17Error.SocketClosed()),
                    attempts,
                    progressUpdates,
                    null,
                )
            }

            ackOutcome = awaitAck(ackTimeoutMs)
            attempts.add(TransmissionAttempt(attemptNumber, ackOutcome.label()))

            when (ackOutcome) {
                is AckOutcome.Acked -> break
                is AckOutcome.Nak, is AckOutcome.AckTimeout -> continue // retransmit same frame
                is AckOutcome.Closed -> return TransmissionOutcome(
                    Ecr17OperationResult.Failure(Ecr17Error.SocketClosed()),
                    attempts,
                    progressUpdates,
                    null,
                )
                AckOutcome.NotAcked -> Unit
            }
        }

        val acked = ackOutcome as? AckOutcome.Acked
            ?: return TransmissionOutcome(
                Ecr17OperationResult.Failure(
                    if (ackOutcome is AckOutcome.Nak) Ecr17Error.NakLimitReached() else Ecr17Error.AckTimeout(),
                ),
                attempts,
                progressUpdates,
                null,
            )

        // From this point on the request has been physically acknowledged: never resend it.
        return awaitFinalResponse(responseTimeoutMs, progressTimeoutMs, onProgress, attempts, progressUpdates, acked.at, operationId)
    }

    private sealed interface AckOutcome {
        data object NotAcked : AckOutcome
        data class Acked(val at: Long) : AckOutcome
        data object Nak : AckOutcome
        data object AckTimeout : AckOutcome
        data object Closed : AckOutcome

        fun label(): String = when (this) {
            is Acked -> "ACK"
            Nak -> "NAK"
            AckTimeout -> "ACK timeout"
            Closed -> "connection closed"
            NotAcked -> "not acked"
        }
    }

    private suspend fun awaitAck(ackTimeoutMs: Int): AckOutcome {
        val deadline = System.currentTimeMillis() + ackTimeoutMs
        while (true) {
            val remaining = (deadline - System.currentTimeMillis()).toInt()
            if (remaining <= 0) return AckOutcome.AckTimeout

            when (val event = transport.receive(remaining)) {
                is TransportEvent.Timeout -> return AckOutcome.AckTimeout
                is TransportEvent.Closed -> return AckOutcome.Closed
                is TransportEvent.Data -> {
                    val packets = parser.append(event.bytes)
                    for (packet in packets) {
                        when (packet) {
                            is Ecr17Packet.Ack -> return AckOutcome.Acked(System.currentTimeMillis())
                            is Ecr17Packet.Nak -> return AckOutcome.Nak
                            else -> Unit // ignore stray progress/application bytes while awaiting ACK
                        }
                    }
                }
            }
        }
    }

    private suspend fun awaitFinalResponse(
        responseTimeoutMs: Int,
        progressTimeoutMs: Int,
        onProgress: suspend (ProgressUpdate) -> Unit,
        attempts: MutableList<TransmissionAttempt>,
        progressUpdates: MutableList<ProgressUpdate>,
        ackReceivedAt: Long,
        operationId: String,
    ): TransmissionOutcome {
        val overallDeadline = System.currentTimeMillis() + responseTimeoutMs
        var lastActivity = System.currentTimeMillis()
        var invalidFrameRetries = 0

        while (true) {
            val now = System.currentTimeMillis()
            val overallRemaining = (overallDeadline - now).toInt()
            val progressRemaining = (lastActivity + progressTimeoutMs - now).toInt()
            val waitMs = minOf(overallRemaining, progressRemaining)

            if (waitMs <= 0) {
                // No connection drop occurred, but no final result arrived either: the ECR
                // cannot tell whether the terminal is still processing. Never report Success.
                return TransmissionOutcome(
                    Ecr17OperationResult.Uncertain(operationId, "No final response received after ACK within the allotted time"),
                    attempts,
                    progressUpdates,
                    ackReceivedAt,
                )
            }

            when (val event = transport.receive(waitMs)) {
                is TransportEvent.Timeout -> Unit // loop again; deadlines re-evaluated above
                is TransportEvent.Closed -> return TransmissionOutcome(
                    Ecr17OperationResult.Uncertain(operationId, "Connection closed after ACK but before the final result"),
                    attempts,
                    progressUpdates,
                    ackReceivedAt,
                )
                is TransportEvent.Data -> {
                    lastActivity = System.currentTimeMillis()
                    for (packet in parser.append(event.bytes)) {
                        when (packet) {
                            is Ecr17Packet.Progress -> progressUpdates.add(ProgressUpdate(packet.message).also { onProgress(it) })
                            is Ecr17Packet.Application -> {
                                transport.send(frameCodec.encodeAck())
                                return TransmissionOutcome(
                                    Ecr17OperationResult.Success(packet.message),
                                    attempts,
                                    progressUpdates,
                                    ackReceivedAt,
                                )
                            }
                            is Ecr17Packet.Invalid -> {
                                invalidFrameRetries++
                                transport.send(frameCodec.encodeNak())
                                if (invalidFrameRetries >= Ecr17Constants.PROTOCOL_MAX_TRANSMISSIONS) {
                                    return TransmissionOutcome(
                                        Ecr17OperationResult.Uncertain(operationId, "Repeated malformed responses after ACK"),
                                        attempts,
                                        progressUpdates,
                                        ackReceivedAt,
                                    )
                                }
                            }
                            is Ecr17Packet.Ack, is Ecr17Packet.Nak -> Unit // stray/late control byte, ignore
                        }
                    }
                }
            }
        }
    }
}
