package com.example.vault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    // Locked Apps Queries
    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllLockedApps(): Flow<List<LockedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLockedApp(app: LockedAppEntity)

    @Delete
    suspend fun deleteLockedApp(app: LockedAppEntity)

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteLockedAppByPackage(packageName: String)

    // Vault Notes Queries
    @Query("SELECT * FROM vault_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<VaultNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: VaultNoteEntity)

    @Delete
    suspend fun deleteNote(note: VaultNoteEntity)

    // Vault Contacts Queries
    @Query("SELECT * FROM vault_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<VaultContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: VaultContactEntity)

    @Delete
    suspend fun deleteContact(contact: VaultContactEntity)

    // Vault Credentials Queries
    @Query("SELECT * FROM vault_credentials ORDER BY serviceName ASC")
    fun getAllCredentials(): Flow<List<VaultCredentialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: VaultCredentialEntity)

    @Delete
    suspend fun deleteCredential(credential: VaultCredentialEntity)

    // Intruder Logs Queries
    @Query("SELECT * FROM intruder_logs ORDER BY timestamp DESC")
    fun getAllIntruderLogs(): Flow<List<IntruderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntruderLog(log: IntruderLogEntity)

    @Query("DELETE FROM intruder_logs")
    suspend fun clearAllIntruderLogs()
}
