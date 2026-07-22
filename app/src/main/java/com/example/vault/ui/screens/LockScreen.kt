package com.example.vault.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.vault.security.BiometricPromptManager
import com.example.vault.ui.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    viewModel: VaultViewModel,
    uiState: com.example.vault.ui.VaultUiState,
    onOpenCalculatorDisguise: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isBackupKeyMode by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryAnswer by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

    // Automatically trigger Biometrics if available and enabled
    LaunchedEffect(Unit) {
        if (uiState.isBiometricEnabled && fragmentActivity != null) {
            val bioManager = BiometricPromptManager(fragmentActivity)
            if (bioManager.canAuthenticate()) {
                bioManager.showBiometricPrompt(
                    title = "HUB Pro Face / Biometric Unlock",
                    subtitle = "Verify identity to access hidden apps & vault",
                    onResult = { result ->
                        if (result is BiometricPromptManager.BiometricResult.AuthenticationSuccess) {
                            viewModel.authenticateWithBiometrics()
                        }
                    }
                )
            }
        }
    }

    fun onNumClick(digit: String) {
        if (pinInput.length < 6) {
            pinInput += digit
            if (pinInput.length >= 4 && !uiState.isTwoFactorEnabled) {
                // Auto verify if 2FA disabled
                viewModel.verifyPin(pinInput)
            }
        }
    }

    fun onDeleteClick() {
        if (pinInput.isNotEmpty()) {
            pinInput = pinInput.dropLast(1)
        }
    }

    fun onClearClick() {
        pinInput = ""
        otpInput = ""
    }

    fun onSubmitUnlock() {
        val success = viewModel.verifyPin(pinInput, otpInput)
        if (!success) {
            pinInput = ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpenCalculatorDisguise,
                    modifier = Modifier.testTag("switch_calculator_disguise_button")
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = "Calculator Disguise",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculator Mode", fontSize = 12.sp)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (uiState.failedPinAttempts > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (uiState.failedPinAttempts > 0) Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = "Status",
                            tint = if (uiState.failedPinAttempts > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.failedPinAttempts > 0) "${uiState.failedPinAttempts} Failed Attempts" else "Secured Vault",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.failedPinAttempts > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Lock Icon & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault Locked",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "HUB Pro Protected",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Enter Master PIN to access hidden apps & data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Error or Info Message Alert
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                val totalDots = 4.coerceAtLeast(pinInput.length.coerceAtMost(6))
                for (i in 0 until totalDots) {
                    val isFilled = i < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            // 2FA Authenticator Field (If 2FA Enabled)
            if (uiState.isTwoFactorEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.VpnKey,
                                    contentDescription = "2FA",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBackupKeyMode) "2FA Emergency Backup Key" else "2FA Authenticator Code",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(
                                onClick = { isBackupKeyMode = !isBackupKeyMode },
                                modifier = Modifier.testTag("toggle_backup_code_button")
                            ) {
                                Text(
                                    text = if (isBackupKeyMode) "Use Authenticator" else "Use Backup Code",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { if (it.length <= 6) otpInput = it },
                            placeholder = { Text(if (isBackupKeyMode) "Enter 6-digit Backup Key" else "Enter 6-digit TOTP Code") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Keypad Pad Grid
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "BIO" -> {
                                    IconButton(
                                        onClick = {
                                            if (fragmentActivity != null) {
                                                val bioManager = BiometricPromptManager(fragmentActivity)
                                                bioManager.showBiometricPrompt(
                                                    title = "HUB Pro Biometric Unlock",
                                                    subtitle = "Face ID or Fingerprint Scan",
                                                    onResult = { result ->
                                                        if (result is BiometricPromptManager.BiometricResult.AuthenticationSuccess) {
                                                            viewModel.authenticateWithBiometrics()
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .testTag("biometric_face_id_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = "Face ID / Fingerprint Unlock",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                "DEL" -> {
                                    IconButton(
                                        onClick = { onDeleteClick() },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .testTag("pin_delete_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { onNumClick(key) }
                                            .testTag("pin_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Submit Button (If 2FA Enabled or PIN length >= 4)
            if (uiState.isTwoFactorEnabled || pinInput.length >= 4) {
                Button(
                    onClick = { onSubmitUnlock() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("unlock_vault_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock Vault", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Forgot PIN / Recovery Option
            TextButton(
                onClick = { showRecoveryDialog = true },
                modifier = Modifier.testTag("forgot_pin_button")
            ) {
                Text(
                    "Forgot PIN or Recovery Option?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Security Question Reset Dialog
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            icon = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
            title = { Text("Vault Security Recovery") },
            text = {
                Column {
                    Text(
                        text = viewModel.securityManager.securityQuestion.ifEmpty { "Security Question: What is your secret recovery key?" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = recoveryAnswer,
                        onValueChange = { recoveryAnswer = it },
                        label = { Text("Your Secret Answer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recovery_answer_input"),
                        singleLine = true
                    )
                    recoveryError?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isAnswerCorrect = viewModel.securityManager.verifySecurityAnswer(recoveryAnswer)
                        if (isAnswerCorrect) {
                            showRecoveryDialog = false
                            viewModel.authenticateWithBiometrics()
                        } else {
                            recoveryError = "Incorrect Security Answer!"
                        }
                    },
                    modifier = Modifier.testTag("verify_recovery_button")
                ) {
                    Text("Verify & Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
