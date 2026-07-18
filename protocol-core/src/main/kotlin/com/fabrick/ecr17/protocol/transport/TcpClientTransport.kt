package com.fabrick.ecr17.protocol.transport

import com.fabrick.ecr17.protocol.core.Ecr17Error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Thrown by [TcpClientTransport] connect/send failures; carries a typed [error] for the caller to map to UI text. */
class Ecr17TransportException(val error: Ecr17Error) : Exception(error.message)

/**
 * Default transport: the Android app is a TCP *client* connecting to the POS terminal
 * (the task's stated default). Built on plain `java.net.Socket` so it has no Android
 * dependency and is unit-testable on the JVM.
 */
class TcpClientTransport(
    private val socketFactory: () -> Socket = { Socket() },
) : PosSocketTransport {

    private var socket: Socket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    override val isConnected: Boolean
        get() = socket?.let { it.isConnected && !it.isClosed } ?: false

    override suspend fun connect(host: String, port: Int, connectTimeoutMs: Int) = withContext(Dispatchers.IO) {
        val newSocket = socketFactory()
        try {
            newSocket.connect(InetSocketAddress(host, port), connectTimeoutMs)
        } catch (e: SocketTimeoutException) {
            throw Ecr17TransportException(Ecr17Error.ConnectionTimeout())
        } catch (e: ConnectException) {
            throw Ecr17TransportException(Ecr17Error.ConnectionRefused())
        } catch (e: UnknownHostException) {
            throw Ecr17TransportException(Ecr17Error.Configuration("Unknown host: $host"))
        } catch (e: IOException) {
            throw Ecr17TransportException(Ecr17Error.Unknown(e.message ?: "connect failed"))
        }
        socket = newSocket
        input = newSocket.getInputStream()
        output = newSocket.getOutputStream()
        Unit
    }

    override suspend fun send(bytes: ByteArray) = withContext(Dispatchers.IO) {
        val out = output ?: throw Ecr17TransportException(Ecr17Error.SocketClosed())
        try {
            out.write(bytes)
            out.flush()
        } catch (e: IOException) {
            throw Ecr17TransportException(Ecr17Error.SocketClosed())
        }
    }

    override suspend fun receive(timeoutMs: Int): TransportEvent = withContext(Dispatchers.IO) {
        val currentSocket = socket ?: return@withContext TransportEvent.Closed
        val inp = input ?: return@withContext TransportEvent.Closed
        try {
            currentSocket.soTimeout = timeoutMs
            val buffer = ByteArray(4096)
            val read = inp.read(buffer)
            if (read == -1) TransportEvent.Closed else TransportEvent.Data(buffer.copyOf(read))
        } catch (e: SocketTimeoutException) {
            TransportEvent.Timeout
        } catch (e: IOException) {
            TransportEvent.Closed
        }
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        runCatching { socket?.close() }
        socket = null
        input = null
        output = null
    }
}
