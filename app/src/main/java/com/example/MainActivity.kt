package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AppVaultTheme
import com.example.vault.ui.VaultViewModel
import com.example.vault.ui.screens.CalculatorDisguiseScreen
import com.example.vault.ui.screens.LockScreen
import com.example.vault.ui.screens.SetupScreen
import com.example.vault.ui.screens.VaultHomeScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: VaultViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    var forcePinScreen by remember { mutableStateOf(false) }

                    when {
                        !uiState.isSetupCompleted -> {
                            SetupScreen(viewModel = viewModel)
                        }
                        !uiState.isUnlocked -> {
                            if (uiState.isCalculatorDisguiseActive && !forcePinScreen) {
                                CalculatorDisguiseScreen(
                                    viewModel = viewModel,
                                    onOpenRealLock = { forcePinScreen = true }
                                )
                            } else {
                                LockScreen(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    onOpenCalculatorDisguise = { forcePinScreen = false }
                                )
                            }
                        }
                        else -> {
                            VaultHomeScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                        }
                    }
                }
            }
        }
    }
}
