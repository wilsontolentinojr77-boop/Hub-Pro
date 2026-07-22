package com.example.vault.data.local

import com.example.vault.apps.AppManager
import com.example.vault.apps.InstalledAppInfo
import com.example.vault.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VaultRepository(
    private val dao: VaultDao,
    private val securityManager: SecurityManager,
    private val appManager: AppManager
) {

    // Room Database Flows
    val allNotes: Flow<List<VaultNoteEntity>> = dao.getAllNotes()
    val allContacts: Flow<List<VaultContactEntity>> = dao.getAllContacts()
    val allCredentials: Flow<List<VaultCredentialEntity>> = dao.getAllCredentials()
    val allIntruderLogs: Flow<List<IntruderLogEntity>> = dao.getAllIntruderLogs()
    val lockedAppEntities: Flow<List<LockedAppEntity>> = dao.getAllLockedApps()

    /**
     * Map installed device apps combined with local lock database state.
     */
    fun getInstalledAppsFlow(): Flow<List<InstalledAppInfo>> {
        return lockedAppEntities.map { entities ->
            appManager.getInstalledApps(entities)
        }
    }

    // App Lock Actions
    suspend fun toggleAppLock(app: InstalledAppInfo, isLocked: Boolean) {
        val entity = LockedAppEntity(
            packageName = app.packageName,
            appName = app.appName,
            isLocked = isLocked,
            isHiddenFromLauncher = app.isHiddenFromLauncher,
            category = app.category
        )
        if (isLocked || app.isHiddenFromLauncher) {
            dao.insertOrUpdateLockedApp(entity)
        } else {
            dao.deleteLockedAppByPackage(app.packageName)
        }
    }

    suspend fun toggleAppHide(app: InstalledAppInfo, isHidden: Boolean) {
        val entity = LockedAppEntity(
            packageName = app.packageName,
            appName = app.appName,
            isLocked = app.isLocked,
            isHiddenFromLauncher = isHidden,
            category = app.category
        )
        if (app.isLocked || isHidden) {
            dao.insertOrUpdateLockedApp(entity)
        } else {
            dao.deleteLockedAppByPackage(app.packageName)
        }
    }

    suspend fun lockCategory(category: String, apps: List<InstalledAppInfo>, shouldLock: Boolean) {
        for (app in apps) {
            if (category == "All" || app.category == category) {
                toggleAppLock(app, shouldLock)
            }
        }
    }

    // Vault Content Actions
    suspend fun insertNote(note: VaultNoteEntity) = dao.insertNote(note)
    suspend fun deleteNote(note: VaultNoteEntity) = dao.deleteNote(note)

    suspend fun insertContact(contact: VaultContactEntity) = dao.insertContact(contact)
    suspend fun deleteContact(contact: VaultContactEntity) = dao.deleteContact(contact)

    suspend fun insertCredential(credential: VaultCredentialEntity) = dao.insertCredential(credential)
    suspend fun deleteCredential(credential: VaultCredentialEntity) = dao.deleteCredential(credential)

    suspend fun logIntruderAttempt(pinAttempt: String) {
        val log = IntruderLogEntity(
            attemptedPin = pinAttempt,
            timestamp = System.currentTimeMillis()
        )
        dao.insertIntruderLog(log)
    }

    suspend fun clearIntruderLogs() = dao.clearAllIntruderLogs()

    fun launchInstalledApp(packageName: String): Boolean {
        return appManager.launchApp(packageName)
    }
}
