package com.example.vault.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLocked: Boolean = true,
    val isHiddenFromLauncher: Boolean = false,
    val category: String = "General",
    val lockedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_notes")
data class VaultNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "vault_contacts")
data class VaultContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val secretNotes: String = ""
)

@Entity(tableName = "vault_credentials")
data class VaultCredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String,
    val username: String,
    val passwordEncrypted: String,
    val notes: String = ""
)

@Entity(tableName = "intruder_logs")
data class IntruderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val attemptedPin: String,
    val sourceDevice: String = "Front Camera",
    val isPhotoCaptured: Boolean = true
)
