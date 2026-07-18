package com.fabrick.ecr17.client.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fabrick.ecr17.client.data.ConnectionConfig
import com.fabrick.ecr17.client.data.ConnectionConfigStore
import com.fabrick.ecr17.protocol.core.Ecr17Error
import com.fabrick.ecr17.protocol.core.Ecr17OperationResult
import com.fabrick.ecr17.protocol.core.MoneyParseResult
import com.fabrick.ecr17.protocol.core.MoneyParser
import com.fabrick.ecr17.protocol.messages.common.CardPresentMode
import com.fabrick.ecr17.protocol.messages.common.PaymentType
import com.fabrick.ecr17.protocol.messages.payment.PaymentRequest
import com.fabrick.ecr17.protocol.messages.payment.PaymentResponse
import com.fabrick.ecr17.protocol.messages.payment.PaymentResponseParser
import com.fabrick.ecr17.protocol.messages.status.PosStatusRequest
import com.fabrick.ecr17.protocol.messages.status.PosStatusResponseParser
import com.fabrick.ecr17.protocol.service.Ecr17TransmissionSession
import com.fabrick.ecr17.protocol.service.TransmissionOutcome
import com.fabrick.ecr17.protocol.transport.PosSocketTransport
import com.fabrick.ecr17.protocol.transport.TcpClientTransport
import com.fabrick.ecr17.protocol.transport.Ecr17TransportException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/** Immutable UI state for the minimal Buy screen. Connection fields are persisted (DataStore); amount/result are not. */
data class BuyUiState(
    val host: String = "",
    val port: String = "",
    val terminalId: String = "",
    val cashRegisterId: String = "",
    val amountText: String = "",
    val isSending: Boolean = false,
    val progressMessage: String? = null,
    val resultText: String? = null,
    val isError: Boolean = false,
    val isTestingConnection: Boolean = false,
    val testConnectionResult: String? = null,
    val testConnectionIsError: Boolean = false,
) {
    val canBuy: Boolean
        get() = !isSending &&
            host.isNotBlank() &&
            port.toIntOrNull() != null &&
            terminalId.length == 8 && terminalId.all { it.isDigit() } &&
            cashRegisterId.length == 8 && cashRegisterId.all { it.isDigit() } &&
            MoneyParser.parse(amountText) is MoneyParseResult.Valid

    val canTestConnection: Boolean
        get() = !isSending && !isTestingConnection && host.isNotBlank() && port.toIntOrNull() != null
}

/**
 * Drives the minimal "send a payment, show the response" flow directly against
 * `:protocol-core` (no transaction history/Room yet — that lands in a later phase).
 *
 * The four connection fields (host/port/Terminal ID/Cash Register ID) are persisted via
 * [ConnectionConfigStore] so they survive an app restart; everything else (amount, result)
 * stays in-memory only.
 */
class BuyViewModel(application: Application) : AndroidViewModel(application) {

    private val configStore = ConnectionConfigStore(application)

    private val _uiState = MutableStateFlow(BuyUiState())
    val uiState: StateFlow<BuyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = configStore.config.first()
            _uiState.update {
                it.copy(
                    host = saved.host,
                    port = saved.port,
                    terminalId = saved.terminalId,
                    cashRegisterId = saved.cashRegisterId,
                )
            }
        }
    }

    fun onHostChange(value: String) {
        _uiState.update { it.copy(host = value) }
        persistConnectionConfig()
    }

    fun onPortChange(value: String) {
        _uiState.update { it.copy(port = value.filter { c -> c.isDigit() }) }
        persistConnectionConfig()
    }

    fun onTerminalIdChange(value: String) {
        _uiState.update { it.copy(terminalId = value.filter { c -> c.isDigit() }.take(8)) }
        persistConnectionConfig()
    }

    fun onCashRegisterIdChange(value: String) {
        _uiState.update { it.copy(cashRegisterId = value.filter { c -> c.isDigit() }.take(8)) }
        persistConnectionConfig()
    }

    fun onAmountChange(value: String) = _uiState.update { it.copy(amountText = value) }

    private fun persistConnectionConfig() {
        val state = _uiState.value
        viewModelScope.launch {
            configStore.save(
                ConnectionConfig(
                    host = state.host,
                    port = state.port,
                    terminalId = state.terminalId,
                    cashRegisterId = state.cashRegisterId,
                ),
            )
        }
    }

    /**
     * Verifies only that a raw TCP socket can be opened to host:port — no Protocol 17
     * message is sent at all. Lets you isolate "can the phone reach the terminal on the
     * network" from "does the terminal answer Protocol 17 messages" (an ACK timeout means
     * the socket connected fine but the peer never acknowledged the framed message, which
     * is a different problem than this check covers).
     */
    fun onTestConnectionClick() {
        val state = _uiState.value
        if (!state.canTestConnection) return
        val port = state.port.toIntOrNull() ?: return

        _uiState.update { it.copy(isTestingConnection = true, testConnectionResult = null, testConnectionIsError = false) }

        viewModelScope.launch(Dispatchers.IO) {
            val transport: PosSocketTransport = TcpClientTransport()
            try {
                transport.connect(state.host, port, connectTimeoutMs = 5000)
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testConnectionResult = "Connected to ${state.host}:$port. " +
                            "This only confirms the TCP socket opened — it does not send any Protocol 17 message " +
                            "or confirm PAXTools/ECR is listening on this port.",
                        testConnectionIsError = false,
                    )
                }
            } catch (e: Ecr17TransportException) {
                _uiState.update {
                    it.copy(isTestingConnection = false, testConnectionResult = Ecr17Error.userMessage(e.error), testConnectionIsError = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isTestingConnection = false, testConnectionResult = "Unexpected error: ${e.message}", testConnectionIsError = true)
                }
            } finally {
                withContext(Dispatchers.IO) { transport.close() }
            }
        }
    }

    fun onBuyClick() {
        val state = _uiState.value
        if (!state.canBuy) return

        val port = state.port.toIntOrNull() ?: return
        val amount = (MoneyParser.parse(state.amountText) as? MoneyParseResult.Valid)?.minorUnits ?: return

        _uiState.update { it.copy(isSending = true, resultText = null, isError = false, progressMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val transport: PosSocketTransport = TcpClientTransport()
            try {
                transport.connect(state.host, port, connectTimeoutMs = 5000)

                val statusOutcome = runOperation(
                    transport = transport,
                    applicationMessage = PosStatusRequest(state.terminalId).applicationMessage(),
                    ackTimeoutMs = 3000,
                    responseTimeoutMs = 10000,
                    progressTimeoutMs = 10000,
                )
                val statusResponseMessage = (statusOutcome.result as? Ecr17OperationResult.Success)?.response
                if (statusResponseMessage == null) {
                    finish(describeNonSuccess(statusOutcome.result), isError = true)
                    return@launch
                }

                val status = PosStatusResponseParser.parse(statusResponseMessage)
                if (!status.status.operative) {
                    finish("POS not operative: ${status.status.description}", isError = true)
                    return@launch
                }

                val paymentRequest = PaymentRequest(
                    terminalId = state.terminalId,
                    cashRegisterId = state.cashRegisterId,
                    cardPresent = CardPresentMode.NOT_YET_PRESENT,
                    paymentType = PaymentType.AUTO_RECOGNITION,
                    amountMinorUnits = amount,
                )
                val paymentOutcome = runOperation(
                    transport = transport,
                    applicationMessage = paymentRequest.applicationMessage(),
                    ackTimeoutMs = 5000,
                    responseTimeoutMs = 90000,
                    progressTimeoutMs = 20000,
                    onProgress = { update -> _uiState.update { it.copy(progressMessage = update.message.trim()) } },
                )

                when (val result = paymentOutcome.result) {
                    is Ecr17OperationResult.Success -> {
                        val response = PaymentResponseParser.parse(result.response)
                        finish(describePayment(response), isError = response is PaymentResponse.Denied)
                    }
                    else -> finish(describeNonSuccess(result), isError = true)
                }
            } catch (e: Ecr17TransportException) {
                finish(Ecr17Error.userMessage(e.error), isError = true)
            } catch (e: Exception) {
                finish("Unexpected error: ${e.message}", isError = true)
            } finally {
                withContext(Dispatchers.IO) { transport.close() }
            }
        }
    }

    private suspend fun runOperation(
        transport: PosSocketTransport,
        applicationMessage: String,
        ackTimeoutMs: Int,
        responseTimeoutMs: Int,
        progressTimeoutMs: Int,
        onProgress: suspend (com.fabrick.ecr17.protocol.service.ProgressUpdate) -> Unit = {},
    ): TransmissionOutcome {
        val session = Ecr17TransmissionSession(transport)
        return session.execute(
            operationId = UUID.randomUUID().toString(),
            applicationMessage = applicationMessage,
            ackTimeoutMs = ackTimeoutMs,
            responseTimeoutMs = responseTimeoutMs,
            progressTimeoutMs = progressTimeoutMs,
            onProgress = onProgress,
        )
    }

    private fun describeNonSuccess(result: Ecr17OperationResult<String>): String = when (result) {
        is Ecr17OperationResult.Failure -> Ecr17Error.userMessage(result.error)
        is Ecr17OperationResult.Uncertain ->
            "The POS acknowledged this request, but the final result was not received. " +
                "Verify the terminal and receipt before attempting the operation again. (${result.reason})"
        is Ecr17OperationResult.Rejected -> "Rejected by terminal."
        is Ecr17OperationResult.Success -> "" // unreachable from call sites that already handled Success
    }

    private fun describePayment(response: PaymentResponse): String = when (response) {
        is PaymentResponse.Approved -> buildString {
            appendLine("APPROVED")
            appendLine("Card: ${response.maskedPan}")
            appendLine("Auth code: ${response.authorizationCode}")
            appendLine("STAN: ${response.trailer.stan}")
            append("Online ID: ${response.trailer.idOnline}")
        }
        is PaymentResponse.Denied -> "DECLINED\n${response.resultDescription}"
    }

    private fun finish(text: String, isError: Boolean) {
        _uiState.update { it.copy(isSending = false, resultText = text, isError = isError, progressMessage = null) }
    }
}
