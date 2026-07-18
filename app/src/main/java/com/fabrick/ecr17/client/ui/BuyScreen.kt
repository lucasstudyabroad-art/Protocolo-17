package com.fabrick.ecr17.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Minimal Buy screen: connection fields, an amount, and a Buy button that sends a Basic
 * Payment and shows the parsed response. No history, no "Other operations" yet.
 */
@Composable
fun BuyScreen(viewModel: BuyViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Buy", style = MaterialTheme.typography.headlineMedium)

        Text("POS connection", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.host,
            onValueChange = viewModel::onHostChange,
            label = { Text("POS IP address / host") },
            singleLine = true,
            enabled = !state.isSending,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.port,
            onValueChange = viewModel::onPortChange,
            label = { Text("POS TCP port") },
            singleLine = true,
            enabled = !state.isSending,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.terminalId,
            onValueChange = viewModel::onTerminalIdChange,
            label = { Text("Terminal ID (8 digits)") },
            singleLine = true,
            enabled = !state.isSending,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.cashRegisterId,
            onValueChange = viewModel::onCashRegisterIdChange,
            label = { Text("Cash Register ID (8 digits)") },
            singleLine = true,
            enabled = !state.isSending,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        Text("Amount", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.amountText,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount (e.g. 6,50)") },
            singleLine = true,
            enabled = !state.isSending,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = viewModel::onBuyClick,
            enabled = state.canBuy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("BUY")
        }

        if (state.isSending) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator()
                Text(state.progressMessage ?: "Waiting for POS...")
            }
        }

        state.resultText?.let { text ->
            HorizontalDivider()
            Text(
                text = text,
                color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
    }
}
