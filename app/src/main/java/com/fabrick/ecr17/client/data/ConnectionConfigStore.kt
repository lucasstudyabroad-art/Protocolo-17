package com.fabrick.ecr17.client.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.connectionDataStore by preferencesDataStore(name = "connection_config")

/** POS connection parameters only — no transaction data. Terminal ID / Cash Register ID are stored as strings to preserve leading zeros. */
data class ConnectionConfig(
    val host: String = "",
    val port: String = "",
    val terminalId: String = "",
    val cashRegisterId: String = "",
)

/**
 * Persists just the four fields on the Buy screen so they survive an app restart. This is
 * deliberately separate from (and much smaller than) a full Configuration/repository layer —
 * transaction history and the rest of the settings screen are still deferred.
 */
class ConnectionConfigStore(private val context: Context) {

    private object Keys {
        val HOST = stringPreferencesKey("host")
        val PORT = stringPreferencesKey("port")
        val TERMINAL_ID = stringPreferencesKey("terminal_id")
        val CASH_REGISTER_ID = stringPreferencesKey("cash_register_id")
    }

    val config: Flow<ConnectionConfig> = context.connectionDataStore.data.map { prefs ->
        ConnectionConfig(
            host = prefs[Keys.HOST] ?: "",
            port = prefs[Keys.PORT] ?: "",
            terminalId = prefs[Keys.TERMINAL_ID] ?: "",
            cashRegisterId = prefs[Keys.CASH_REGISTER_ID] ?: "",
        )
    }

    suspend fun save(config: ConnectionConfig) {
        context.connectionDataStore.edit { prefs ->
            prefs[Keys.HOST] = config.host
            prefs[Keys.PORT] = config.port
            prefs[Keys.TERMINAL_ID] = config.terminalId
            prefs[Keys.CASH_REGISTER_ID] = config.cashRegisterId
        }
    }
}
