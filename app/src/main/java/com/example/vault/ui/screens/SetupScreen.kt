package com.example.vault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vault.ui.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: VaultViewModel
) {
    var step by remember { mutableStateOf(1) }
    var masterPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("What was the name of your first pet?") }
    var securityAnswer by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

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
            // Header Progress Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Step $step of 2: Setup Vault Guard",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (step == 1) Icons.Default.Lock else Icons.Default.Security,
                        contentDescription = "Setup",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (step == 1) "Create Master PIN" else "Security Recovery Question",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (step == 1) "This PIN will be required to unlock your hidden apps & private vault."
                    else "Used to recover access if you ever forget your Master PIN.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Step Content Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (step == 1) {
                        OutlinedTextField(
                            value = masterPin,
                            onValueChange = { if (it.length <= 6) masterPin = it },
                            label = { Text("Set 4-6 Digit Master PIN") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_master_pin_input"),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { if (it.length <= 6) confirmPin = it },
                            label = { Text("Confirm Master PIN") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_confirm_pin_input"),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = securityQuestion,
                            onValueChange = { securityQuestion = it },
                            label = { Text("Security Recovery Question") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_security_question_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = securityAnswer,
                            onValueChange = { securityAnswer = it },
                            label = { Text("Your Answer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_security_answer_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    validationError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step == 2) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("setup_back_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (step == 1) {
                            if (masterPin.length < 4) {
                                validationError = "PIN must be at least 4 digits"
                            } else if (masterPin != confirmPin) {
                                validationError = "PINs do not match!"
                            } else {
                                validationError = null
                                step = 2
                            }
                        } else {
                            if (securityAnswer.isBlank()) {
                                validationError = "Security answer cannot be blank"
                            } else {
                                viewModel.setupMasterPin(masterPin, securityQuestion, securityAnswer)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("setup_next_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (step == 1) "Continue" else "Initialize Vault",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (step == 1) Icons.Default.ArrowForward else Icons.Default.Check,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
