package com.example.vault.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vault.ui.VaultTab
import com.example.vault.ui.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    viewModel: VaultViewModel,
    uiState: com.example.vault.ui.VaultUiState
) {
    val selectedTab = uiState.selectedTab

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("HUB Pro Guard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (uiState.isTwoFactorEnabled) "2FA Enforced • Biometric Active" else "Protected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.lockVault() },
                        modifier = Modifier.testTag("lock_vault_top_button")
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Lock Vault",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == VaultTab.Overview,
                    onClick = { viewModel.selectTab(VaultTab.Overview) },
                    modifier = Modifier.testTag("nav_overview_tab"),
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                    label = { Text("Overview", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == VaultTab.AppHider,
                    onClick = { viewModel.selectTab(VaultTab.AppHider) },
                    modifier = Modifier.testTag("nav_app_hider_tab"),
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.lockedAppsCount > 0) {
                                    Badge { Text("${uiState.lockedAppsCount}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = "App Hider")
                        }
                    },
                    label = { Text("App Hider", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == VaultTab.SecretVault,
                    onClick = { viewModel.selectTab(VaultTab.SecretVault) },
                    modifier = Modifier.testTag("nav_secret_vault_tab"),
                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Secret Vault") },
                    label = { Text("Vault", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == VaultTab.TwoFactorSetup,
                    onClick = { viewModel.selectTab(VaultTab.TwoFactorSetup) },
                    modifier = Modifier.testTag("nav_2fa_tab"),
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = "2FA") },
                    label = { Text("2FA Auth", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == VaultTab.IntruderLogs,
                    onClick = { viewModel.selectTab(VaultTab.IntruderLogs) },
                    modifier = Modifier.testTag("nav_intruders_tab"),
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.intruderLogs.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) { Text("${uiState.intruderLogs.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Intruders")
                        }
                    },
                    label = { Text("Intruders", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == VaultTab.Settings,
                    onClick = { viewModel.selectTab(VaultTab.Settings) },
                    modifier = Modifier.testTag("nav_settings_tab"),
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 10.sp) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                VaultTab.Overview -> OverviewTabContent(viewModel, uiState)
                VaultTab.AppHider -> AppHiderScreen(viewModel, uiState)
                VaultTab.SecretVault -> SecretVaultScreen(viewModel, uiState)
                VaultTab.TwoFactorSetup -> TwoFactorSetupScreen(viewModel, uiState)
                VaultTab.IntruderLogs -> IntruderLogScreen(viewModel, uiState)
                VaultTab.Settings -> SettingsScreen(viewModel, uiState)
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    viewModel: VaultViewModel,
    uiState: com.example.vault.ui.VaultUiState
) {
    val totalVaultItems = uiState.notes.size + uiState.contacts.size + uiState.credentials.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security Score Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Vault Protection Score",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                if (uiState.isTwoFactorEnabled) "98% Ultra Secure" else "82% High Security",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(
                            label = "Master PIN: Active",
                            isActive = true
                        )
                        StatusChip(
                            label = if (uiState.isBiometricEnabled) "Face/Biometric: On" else "Biometrics: Off",
                            isActive = uiState.isBiometricEnabled
                        )
                        StatusChip(
                            label = if (uiState.isTwoFactorEnabled) "2FA: Enforced" else "2FA: Off",
                            isActive = uiState.isTwoFactorEnabled
                        )
                    }
                }
            }
        }

        // Intruder Alert Banner (If attempts logged)
        if (uiState.intruderLogs.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Intruder Break-In Detected!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("${uiState.intruderLogs.size} failed unlock attempt(s) recorded.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                        }
                        TextButton(
                            onClick = { viewModel.selectTab(VaultTab.IntruderLogs) },
                            modifier = Modifier.testTag("view_intruder_logs_button")
                        ) {
                            Text("View Log", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Metric Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Locked Apps",
                    value = "${uiState.lockedAppsCount}",
                    icon = Icons.Default.Lock,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(VaultTab.AppHider) }
                )
                MetricCard(
                    title = "Hidden Apps",
                    value = "${uiState.hiddenAppsCount}",
                    icon = Icons.Default.VisibilityOff,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(VaultTab.AppHider) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Secret Vault Data",
                    value = "$totalVaultItems Items",
                    icon = Icons.Default.FolderSpecial,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(VaultTab.SecretVault) }
                )
                MetricCard(
                    title = "2FA Authenticator",
                    value = if (uiState.isTwoFactorEnabled) "Enabled" else "Setup Required",
                    icon = Icons.Default.VpnKey,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(VaultTab.TwoFactorSetup) }
                )
            }
        }

        // Quick Category Protection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Quick App Locking Presets", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Instantly secure sensitive app categories with 1 click.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.lockAllCategoryApps("Social", true) },
                            modifier = Modifier.weight(1f).testTag("quick_lock_social_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Social Apps", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.lockAllCategoryApps("Banking & Finance", true) },
                            modifier = Modifier.weight(1f).testTag("quick_lock_banking_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Banking Apps", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, isActive: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
