package com.fabrick.ecr17.protocol.service

import com.fabrick.ecr17.protocol.core.Ecr17OperationResult
import com.fabrick.ecr17.protocol.frame.Ecr17FrameCodec
import com.fabrick.ecr17.protocol.transport.TransportEvent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Ecr17TransmissionSessionTest {

    private val codec = Ecr17FrameCodec()

    private fun progressFrame(text: String): ByteArray {
        val padded = text.padEnd(20).take(20)
        return byteArrayOf(0x01) + padded.toByteArray(Charsets.US_ASCII) + byteArrayOf(0x04)
    }

    /** Number of *request* frames (STX-prefixed) we sent — excludes the ACK/NAK control replies we also send. */
    private fun FakeTransport.requestFrameCount(): Int = sentFrames.count { it.isNotEmpty() && it[0] == 0x02.toByte() }

    @Test
    fun `success path sends request once, gets ACK then response, and ACKs the response`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeAck()),
                TransportEvent.Data(codec.encodeApplication("0000000100E")),
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        val outcome = session.execute(
            operationId = "op-1",
            applicationMessage = "s",
            ackTimeoutMs = 1000,
            responseTimeoutMs = 5000,
            progressTimeoutMs = 5000,
        )

        assertTrue(outcome.result is Ecr17OperationResult.Success)
        assertEquals("0000000100E", (outcome.result as Ecr17OperationResult.Success).response)
        assertEquals(1, transport.requestFrameCount()) // request sent exactly once
        assertEquals(2, transport.sentFrames.size) // request + our ACK of the terminal's response
    }

    @Test
    fun `retransmits same frame after NAK then succeeds`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeNak()),
                TransportEvent.Data(codec.encodeAck()),
                TransportEvent.Data(codec.encodeApplication("0000000100E")),
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        val outcome = session.execute("op-2", "s", 1000, 5000, 5000)

        assertTrue(outcome.result is Ecr17OperationResult.Success)
        assertEquals(2, transport.requestFrameCount()) // original + one retransmission
        // both transmitted request frames are byte-identical (same exact frame, not rebuilt)
        assertTrue(transport.sentFrames[0].contentEquals(transport.sentFrames[1]))
    }

    @Test
    fun `retransmits after ACK timeout then succeeds`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Timeout,
                TransportEvent.Data(codec.encodeAck()),
                TransportEvent.Data(codec.encodeApplication("0000000100E")),
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        val outcome = session.execute("op-3", "s", 50, 5000, 5000)

        assertTrue(outcome.result is Ecr17OperationResult.Success)
        assertEquals(2, transport.requestFrameCount())
    }

    @Test
    fun `stops after maximum transmissions with repeated NAK`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeNak()),
                TransportEvent.Data(codec.encodeNak()),
                TransportEvent.Data(codec.encodeNak()),
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        val outcome = session.execute("op-4", "s", 1000, 5000, 5000)

        assertTrue(outcome.result is Ecr17OperationResult.Failure)
        assertEquals(3, transport.sentFrames.size) // exactly the protocol maximum, never more
    }

    @Test
    fun `no duplicate request is ever sent once ACK has been received`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeAck()),
                TransportEvent.Timeout,
                TransportEvent.Timeout,
                TransportEvent.Data(codec.encodeApplication("0000000100E")),
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        session.execute("op-5", "s", 1000, 5000, 5000)

        assertEquals(1, transport.requestFrameCount())
    }

    @Test
    fun `marks operation uncertain when connection closes after ACK but before final result`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeAck()),
                TransportEvent.Closed,
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        val outcome = session.execute("op-6", "s", 1000, 5000, 5000)

        assertTrue(outcome.result is Ecr17OperationResult.Uncertain)
        assertEquals("op-6", (outcome.result as Ecr17OperationResult.Uncertain).operationId)
    }

    @Test
    fun `marks operation uncertain when response never arrives within timeout after ACK`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeAck()),
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        val outcome = session.execute("op-7", "s", 1000, 50, 50)

        assertTrue(outcome.result is Ecr17OperationResult.Uncertain)
    }

    @Test
    fun `captures progress updates received between ACK and final response`() = runTest {
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeAck()),
                TransportEvent.Data(progressFrame("Contacting host...")),
                TransportEvent.Data(progressFrame("Waiting for card...")),
                TransportEvent.Data(codec.encodeApplication("0000000100E")),
            ),
        )
        val session = Ecr17TransmissionSession(transport)
        val observed = mutableListOf<String>()

        val outcome = session.execute("op-8", "s", 1000, 5000, 5000, onProgress = { observed.add(it.message.trim()) })

        assertTrue(outcome.result is Ecr17OperationResult.Success)
        assertEquals(2, outcome.progressUpdates.size)
        assertEquals(listOf("Contacting host...", "Waiting for card..."), observed)
    }

    @Test
    fun `NAKs an invalid frame received after ACK and keeps waiting`() = runTest {
        val corrupted = codec.encodeApplication("s").also { it[it.size - 1] = (it[it.size - 1].toInt() xor 0xFF).toByte() }
        val transport = FakeTransport(
            mutableListOf(
                TransportEvent.Data(codec.encodeAck()),
                TransportEvent.Data(corrupted),
                TransportEvent.Data(codec.encodeApplication("0000000100E")),
            ),
        )
        val session = Ecr17TransmissionSession(transport)

        val outcome = session.execute("op-9", "s", 1000, 5000, 5000)

        assertTrue(outcome.result is Ecr17OperationResult.Success)
        // sentFrames: [request, NAK-for-corrupted-frame, ACK-for-final-response]
        assertEquals(3, transport.sentFrames.size)
    }
}
