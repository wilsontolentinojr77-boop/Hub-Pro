package com.example.vault.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_vault_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SETUP_COMPLETED = "setup_completed"
        private const val KEY_MASTER_PIN_HASH = "master_pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_2FA_ENABLED = "2fa_enabled"
        private const val KEY_2FA_SECRET = "2fa_secret"
        private const val KEY_2FA_BACKUP_CODES = "2fa_backup_codes"
        private const val KEY_CALCULATOR_DISGUISE = "calculator_disguise"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_SECURITY_QUESTION = "security_question"
        private const val KEY_SECURITY_ANSWER_HASH = "security_answer_hash"
    }

    var isSetupCompleted: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETED, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isTwoFactorEnabled: Boolean
        get() = prefs.getBoolean(KEY_2FA_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_2FA_ENABLED, value).apply()

    var twoFactorSecretKey: String
        get() = prefs.getString(KEY_2FA_SECRET, "") ?: ""
        set(value) = prefs.edit().putString(KEY_2FA_SECRET, value).apply()

    var twoFactorBackupCodes: Set<String>
        get() = prefs.getStringSet(KEY_2FA_BACKUP_CODES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_2FA_BACKUP_CODES, value).apply()

    var isCalculatorDisguiseEnabled: Boolean
        get() = prefs.getBoolean(KEY_CALCULATOR_DISGUISE, false)
        set(value) = prefs.edit().putBoolean(KEY_CALCULATOR_DISGUISE, value).apply()

    var failedAttemptsCount: Int
        get() = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        set(value) = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, value).apply()

    var securityQuestion: String
        get() = prefs.getString(KEY_SECURITY_QUESTION, "What is your secret security key?") ?: ""
        set(value) = prefs.edit().putString(KEY_SECURITY_QUESTION, value).apply()

    fun setMasterPin(pin: String) {
        val hash = hashString(pin)
        prefs.edit().putString(KEY_MASTER_PIN_HASH, hash).apply()
    }

    fun verifyMasterPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_MASTER_PIN_HASH, "") ?: ""
        if (storedHash.isEmpty()) return false
        val inputHash = hashString(pin)
        return storedHash == inputHash
    }

    fun setSecurityAnswer(answer: String) {
        val hash = hashString(answer.trim().lowercase())
        prefs.edit().putString(KEY_SECURITY_ANSWER_HASH, hash).apply()
    }

    fun verifySecurityAnswer(answer: String): Boolean {
        val storedHash = prefs.getString(KEY_SECURITY_ANSWER_HASH, "") ?: ""
        if (storedHash.isEmpty()) return false
        val inputHash = hashString(answer.trim().lowercase())
        return storedHash == inputHash
    }

    fun verifyBackupCode(code: String): Boolean {
        val cleanCode = code.trim()
        val currentCodes = twoFactorBackupCodes.toMutableSet()
        if (currentCodes.contains(cleanCode)) {
            // Backup code used: consume it
            currentCodes.remove(cleanCode)
            twoFactorBackupCodes = currentCodes
            return true
        }
        return false
    }

    fun resetAllSecurity() {
        prefs.edit().clear().apply()
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
