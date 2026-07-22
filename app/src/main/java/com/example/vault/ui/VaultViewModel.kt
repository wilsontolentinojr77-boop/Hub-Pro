package com.example.vault.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vault.apps.AppManager
import com.example.vault.apps.InstalledAppInfo
import com.example.vault.data.local.*
import com.example.vault.security.BiometricPromptManager
import com.example.vault.security.SecurityManager
import com.example.vault.security.TotpGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class VaultTab {
    Overview,
    AppHider,
    SecretVault,
    IntruderLogs,
    TwoFactorSetup,
    Settings
}

data class VaultUiState(
    val isUnlocked: Boolean = false,
    val isSetupCompleted: Boolean = false,
    val isCalculatorDisguiseActive: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val isBiometricSupported: Boolean = false,
    val isTwoFactorEnabled: Boolean = false,
    val twoFactorSecretKey: String = "",
    val twoFactorBackupCodes: List<String> = emptyList(),
    val currentTotpCode: String = "",
    val totpRemainingSeconds: Int = 30,
    val selectedTab: VaultTab = VaultTab.Overview,
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val lockedAppsCount: Int = 0,
    val hiddenAppsCount: Int = 0,
    val notes: List<VaultNoteEntity> = emptyList(),
    val contacts: List<VaultContactEntity> = emptyList(),
    val credentials: List<VaultCredentialEntity> = emptyList(),
    val intruderLogs: List<IntruderLogEntity> = emptyList(),
    val failedPinAttempts: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val is2FARequiredForUnlock: Boolean = false
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    val securityManager = SecurityManager(application)
    private val db = VaultDatabase.getDatabase(application)
    private val appManager = AppManager(application)
    val repository = VaultRepository(db.vaultDao(), securityManager, appManager)

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        loadSecurityState()
        startTotpTicker()
        observeData()
    }

    private fun loadSecurityState() {
        val setupDone = securityManager.isSetupCompleted
        val isDisguise = securityManager.isCalculatorDisguiseEnabled
        val isBioEnabled = securityManager.isBiometricEnabled
        val is2FA = securityManager.isTwoFactorEnabled
        val secret = securityManager.twoFactorSecretKey
        val backupCodes = securityManager.twoFactorBackupCodes.toList()

        _uiState.update {
            it.copy(
                isSetupCompleted = setupDone,
                isUnlocked = !setupDone, // Default unlocked if first-time setup
                isCalculatorDisguiseActive = isDisguise,
                isBiometricEnabled = isBioEnabled,
                isTwoFactorEnabled = is2FA,
                twoFactorSecretKey = secret,
                twoFactorBackupCodes = backupCodes,
                is2FARequiredForUnlock = is2FA
            )
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getInstalledAppsFlow().collect { apps ->
                val lockedCount = apps.count { it.isLocked }
                val hiddenCount = apps.count { it.isHiddenFromLauncher }
                _uiState.update {
                    it.copy(
                        installedApps = apps,
                        lockedAppsCount = lockedCount,
                        hiddenAppsCount = hiddenCount
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.allNotes.collect { notesList ->
                _uiState.update { it.copy(notes = notesList) }
            }
        }

        viewModelScope.launch {
            repository.allContacts.collect { contactsList ->
                _uiState.update { it.copy(contacts = contactsList) }
            }
        }

        viewModelScope.launch {
            repository.allCredentials.collect { creds ->
                _uiState.update { it.copy(credentials = creds) }
            }
        }

        viewModelScope.launch {
            repository.allIntruderLogs.collect { logs ->
                _uiState.update { it.copy(intruderLogs = logs) }
            }
        }
    }

    private fun startTotpTicker() {
        viewModelScope.launch {
            while (true) {
                val secret = _uiState.value.twoFactorSecretKey
                if (secret.isNotEmpty()) {
                    val code = TotpGenerator.generateTotpCode(secret)
                    val remaining = TotpGenerator.getRemainingSeconds()
                    _uiState.update {
                        it.copy(currentTotpCode = code, totpRemainingSeconds = remaining)
                    }
                }
                delay(1000)
            }
        }
    }

    // Authentication Actions
    fun setupMasterPin(pin: String, securityQuestion: String, securityAnswer: String) {
        if (pin.length < 4) {
            _uiState.update { it.copy(errorMessage = "PIN must be at least 4 digits") }
            return
        }

        securityManager.setMasterPin(pin)
        securityManager.securityQuestion = securityQuestion
        securityManager.setSecurityAnswer(securityAnswer)
        securityManager.isSetupCompleted = true

        _uiState.update {
            it.copy(
                isSetupCompleted = true,
                isUnlocked = true,
                errorMessage = null,
                successMessage = "Vault initialized successfully!"
            )
        }
    }

    fun verifyPin(pinInput: String, otpInput: String = ""): Boolean {
        if (securityManager.verifyMasterPin(pinInput)) {
            // Check 2FA if enabled
            if (_uiState.value.isTwoFactorEnabled) {
                val secret = _uiState.value.twoFactorSecretKey
                val isOtpValid = TotpGenerator.verifyTotpCode(secret, otpInput) ||
                        securityManager.verifyBackupCode(otpInput)

                if (!isOtpValid) {
                    _uiState.update {
                        it.copy(errorMessage = "Invalid 2FA Authenticator code or Backup Key!")
                    }
                    return false
                }
            }

            // Success unlock
            securityManager.failedAttemptsCount = 0
            _uiState.update {
                it.copy(
                    isUnlocked = true,
                    failedPinAttempts = 0,
                    errorMessage = null,
                    successMessage = "Vault Unlocked"
                )
            }
            return true
        } else {
            // Failed attempt
            val newFailed = _uiState.value.failedPinAttempts + 1
            securityManager.failedAttemptsCount = newFailed

            viewModelScope.launch {
                repository.logIntruderAttempt(pinInput)
            }

            _uiState.update {
                it.copy(
                    failedPinAttempts = newFailed,
                    errorMessage = "Incorrect PIN! Attempt logged."
                )
            }
            return false
        }
    }

    fun verifyCalculatorSecret(expression: String): Boolean {
        // e.g. "1234=" or "1234"
        val clean = expression.replace("=", "").trim()
        if (securityManager.verifyMasterPin(clean)) {
            securityManager.failedAttemptsCount = 0
            _uiState.update {
                it.copy(
                    isUnlocked = true,
                    failedPinAttempts = 0,
                    errorMessage = null,
                    successMessage = "Vault Disguise Unlocked!"
                )
            }
            return true
        }
        return false
    }

    fun authenticateWithBiometrics() {
        securityManager.failedAttemptsCount = 0
        _uiState.update {
            it.copy(
                isUnlocked = true,
                errorMessage = null,
                successMessage = "Biometric / Face Recognition Success!"
            )
        }
    }

    fun lockVault() {
        if (securityManager.isSetupCompleted) {
            _uiState.update { it.copy(isUnlocked = false) }
        }
    }

    // 2FA Setup
    fun generateNew2FASecret() {
        val newSecret = TotpGenerator.generateSecretKey()
        val backupCodes = TotpGenerator.generateBackupCodes()
        _uiState.update {
            it.copy(
                twoFactorSecretKey = newSecret,
                twoFactorBackupCodes = backupCodes
            )
        }
    }

    fun activate2FA(userOtp: String): Boolean {
        val currentSecret = _uiState.value.twoFactorSecretKey
        if (TotpGenerator.verifyTotpCode(currentSecret, userOtp)) {
            securityManager.twoFactorSecretKey = currentSecret
            securityManager.twoFactorBackupCodes = _uiState.value.twoFactorBackupCodes.toSet()
            securityManager.isTwoFactorEnabled = true

            _uiState.update {
                it.copy(
                    isTwoFactorEnabled = true,
                    errorMessage = null,
                    successMessage = "2-Factor Authentication Activated!"
                )
            }
            return true
        } else {
            _uiState.update {
                it.copy(errorMessage = "Invalid 2FA Code. Check your authenticator app.")
            }
            return false
        }
    }

    fun disable2FA() {
        securityManager.isTwoFactorEnabled = false
        securityManager.twoFactorSecretKey = ""
        securityManager.twoFactorBackupCodes = emptySet()
        _uiState.update {
            it.copy(
                isTwoFactorEnabled = false,
                twoFactorSecretKey = "",
                twoFactorBackupCodes = emptyList(),
                successMessage = "2-Factor Authentication Disabled"
            )
        }
    }

    // Settings Toggles
    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.isBiometricEnabled = enabled
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun setCalculatorDisguiseEnabled(enabled: Boolean) {
        securityManager.isCalculatorDisguiseEnabled = enabled
        _uiState.update { it.copy(isCalculatorDisguiseActive = enabled) }
    }

    // App Lock Actions
    fun toggleAppLock(app: InstalledAppInfo) {
        viewModelScope.launch {
            repository.toggleAppLock(app, !app.isLocked)
        }
    }

    fun toggleAppHide(app: InstalledAppInfo) {
        viewModelScope.launch {
            repository.toggleAppHide(app, !app.isHiddenFromLauncher)
        }
    }

    fun lockAllCategoryApps(category: String, shouldLock: Boolean) {
        viewModelScope.launch {
            repository.lockCategory(category, _uiState.value.installedApps, shouldLock)
        }
    }

    fun launchApp(app: InstalledAppInfo) {
        val success = repository.launchInstalledApp(app.packageName)
        if (!success) {
            _uiState.update {
                it.copy(successMessage = "Launched ${app.appName} Sandbox")
            }
        }
    }

    // Secret Vault Actions
    fun addNote(title: String, content: String, category: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertNote(VaultNoteEntity(title = title, content = content, category = category))
        }
    }

    fun deleteNote(note: VaultNoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun addContact(name: String, phone: String, email: String, notes: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertContact(VaultContactEntity(name = name, phoneNumber = phone, email = email, secretNotes = notes))
        }
    }

    fun deleteContact(contact: VaultContactEntity) {
        viewModelScope.launch { repository.deleteContact(contact) }
    }

    fun addCredential(service: String, user: String, pass: String, notes: String) {
        if (service.isBlank()) return
        viewModelScope.launch {
            repository.insertCredential(VaultCredentialEntity(serviceName = service, username = user, passwordEncrypted = pass, notes = notes))
        }
    }

    fun deleteCredential(cred: VaultCredentialEntity) {
        viewModelScope.launch { repository.deleteCredential(cred) }
    }

    fun clearIntruderLogs() {
        viewModelScope.launch { repository.clearIntruderLogs() }
    }

    // UI State Helpers
    fun selectTab(tab: VaultTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
