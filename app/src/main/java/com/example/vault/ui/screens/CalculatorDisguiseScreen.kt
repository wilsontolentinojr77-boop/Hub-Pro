package com.example.vault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vault.ui.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorDisguiseScreen(
    viewModel: VaultViewModel,
    onOpenRealLock: () -> Unit
) {
    var displayExpression by remember { mutableStateOf("0") }
    var calcResult by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun onKeyClick(key: String) {
        when (key) {
            "C" -> {
                displayExpression = "0"
                calcResult = ""
                isError = false
            }
            "DEL" -> {
                if (displayExpression.length > 1) {
                    displayExpression = displayExpression.dropLast(1)
                } else {
                    displayExpression = "0"
                }
            }
            "=" -> {
                // Check if secret PIN unlock
                val isUnlocked = viewModel.verifyCalculatorSecret(displayExpression)
                if (!isUnlocked) {
                    // Evaluate standard math calculation
                    try {
                        val evaluated = evaluateSimpleMath(displayExpression)
                        calcResult = evaluated
                    } catch (e: Exception) {
                        calcResult = "Error"
                        isError = true
                    }
                }
            }
            else -> {
                if (displayExpression == "0" && key != ".") {
                    displayExpression = key
                } else {
                    displayExpression += key
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Calculator", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                "Stealth Mode",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenRealLock,
                        modifier = Modifier.testTag("open_pin_pad_button")
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Switch to PIN Lock",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Calculator Screen Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = displayExpression,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 2
                    )
                    if (calcResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "= $calcResult",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enter secret PIN and press '=' to unlock vault",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Button Keypad Grid
            val buttons = listOf(
                listOf("C", "DEL", "%", "/"),
                listOf("7", "8", "9", "*"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            val isOperator = key in listOf("/", "*", "-", "+", "=")
                            val isAction = key in listOf("C", "DEL", "%")
                            val weight = if (key == "=") 2f else 1f

                            CalcButton(
                                text = key,
                                modifier = Modifier
                                    .weight(weight)
                                    .aspectRatio(if (key == "=") 2.1f else 1f)
                                    .testTag("calc_key_$key"),
                                isOperator = isOperator,
                                isAction = isAction,
                                onClick = { onKeyClick(key) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isAction: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        keyEquals(text) -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.secondary
        isAction -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        keyEquals(text) -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onSecondary
        isAction -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

private fun keyEquals(text: String) = text == "="

private fun evaluateSimpleMath(expr: String): String {
    // Basic safe arithmetic parser
    val clean = expr.replace(" ", "")
    if (clean.contains("+")) {
        val parts = clean.split("+")
        val sum = parts.sumOf { it.toDoubleOrNull() ?: 0.0 }
        return if (sum % 1.0 == 0.0) sum.toLong().toString() else sum.toString()
    } else if (clean.contains("-")) {
        val parts = clean.split("-")
        val first = parts.firstOrNull()?.toDoubleOrNull() ?: 0.0
        val rest = parts.drop(1).sumOf { it.toDoubleOrNull() ?: 0.0 }
        val diff = first - rest
        return if (diff % 1.0 == 0.0) diff.toLong().toString() else diff.toString()
    } else if (clean.contains("*")) {
        val parts = clean.split("*")
        val prod = parts.map { it.toDoubleOrNull() ?: 1.0 }.reduce { acc, d -> acc * d }
        return if (prod % 1.0 == 0.0) prod.toLong().toString() else prod.toString()
    } else if (clean.contains("/")) {
        val parts = clean.split("/")
        if (parts.size >= 2) {
            val num = parts[0].toDoubleOrNull() ?: 0.0
            val den = parts[1].toDoubleOrNull() ?: 1.0
            if (den == 0.0) return "Error"
            val div = num / den
            return if (div % 1.0 == 0.0) div.toLong().toString() else div.toString()
        }
    }
    return expr
}
