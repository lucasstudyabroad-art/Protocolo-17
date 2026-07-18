package com.fabrick.ecr17.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fabrick.ecr17.client.ui.BuyScreen

/**
 * Single-activity app. Currently hosts only the Buy screen (minimal end-to-end path: send a
 * Basic Payment, show the parsed response). Bottom navigation with History/Configuration is
 * intentionally deferred until transaction persistence is built.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BuyScreen()
                }
            }
        }
    }
}
