package com.example.vault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vault.ui.VaultTab
import com.example.vault.ui.VaultViewModel

@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    uiState: com.example.vault.ui.VaultUiState
) {
    var showChangePinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var confirmNewPinInput by remember { mutableStateOf("") }
    var changePinError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security Preferences Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Authentication & Security", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Biometrics Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Face ID / Biometric Unlock", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Unlock vault using device biometrics", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) },
                            modifier = Modifier.testTag("toggle_biometrics_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 2FA Setup Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("2-Factor Authentication (2FA)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(if (uiState.isTwoFactorEnabled) "Active with TOTP Authenticator" else "Setup Google Authenticator / Authy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        TextButton(
                            onClick = { viewModel.selectTab(VaultTab.TwoFactorSetup) },
                            modifier = Modifier.testTag("manage_2fa_button")
                        ) {
                            Text(if (uiState.isTwoFactorEnabled) "Manage" else "Setup")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Calculator Disguise Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Calculator Disguise Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Opening app displays fake functional calculator", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = uiState.isCalculatorDisguiseActive,
                            onCheckedChange = { viewModel.setCalculatorDisguiseEnabled(it) },
                            modifier = Modifier.testTag("toggle_calculator_disguise_switch")
                        )
                    }
                }
            }
        }

        // Credentials & Lock Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Master Credentials", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("change_master_pin_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Pin, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Master PIN")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.lockVault() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("relock_vault_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Vault Immediately")
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Master PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6) newPinInput = it },
                        label = { Text("New 4-6 Digit PIN") },
                        modifier = Modifier.fillMaxWidth().testTag("new_pin_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmNewPinInput,
                        onValueChange = { if (it.length <= 6) confirmNewPinInput = it },
                        label = { Text("Confirm New PIN") },
                        modifier = Modifier.fillMaxWidth().testTag("confirm_new_pin_input"),
                        singleLine = true
                    )
                    changePinError?.let { err ->
                        Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length < 4) {
                            changePinError = "PIN must be at least 4 digits"
                        } else if (newPinInput != confirmNewPinInput) {
                            changePinError = "PINs do not match!"
                        } else {
                            viewModel.securityManager.setMasterPin(newPinInput)
                            showChangePinDialog = false
                            newPinInput = ""
                            confirmNewPinInput = ""
                            changePinError = null
                        }
                    },
                    modifier = Modifier.testTag("save_new_pin_button")
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("Cancel") }
            }
        )
    }
}
