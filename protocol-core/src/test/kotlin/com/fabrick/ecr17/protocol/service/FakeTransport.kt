package com.fabrick.ecr17.protocol.service

import com.fabrick.ecr17.protocol.transport.PosSocketTransport
import com.fabrick.ecr17.protocol.transport.TransportEvent
import kotlinx.coroutines.delay

/**
 * Test double for [PosSocketTransport]. [scriptedEvents] is consumed in order by
 * successive [receive] calls; a scripted [TransportEvent.Timeout] actually suspends for
 * the requested duration (via [delay]) so tests exercise real timeout-driven code paths
 * under `kotlinx-coroutines-test`'s virtual time, instead of busy-looping.
 */
class FakeTransport(
    private val scriptedEvents: MutableList<TransportEvent> = mutableListOf(),
) : PosSocketTransport {

    val sentFrames = mutableListOf<ByteArray>()
    override var isConnected: Boolean = true
        private set

    override suspend fun connect(host: String, port: Int, connectTimeoutMs: Int) {
        isConnected = true
    }

    override suspend fun send(bytes: ByteArray) {
        sentFrames.add(bytes)
    }

    override suspend fun receive(timeoutMs: Int): TransportEvent {
        if (scriptedEvents.isEmpty()) {
            delay(timeoutMs.toLong())
            return TransportEvent.Timeout
        }
        val next = scriptedEvents.removeAt(0)
        if (next is TransportEvent.Timeout) {
            delay(timeoutMs.toLong())
        }
        return next
    }

    override suspend fun close() {
        isConnected = false
    }
}
