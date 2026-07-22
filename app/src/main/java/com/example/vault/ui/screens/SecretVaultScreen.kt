package com.example.vault.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vault.data.local.VaultContactEntity
import com.example.vault.data.local.VaultCredentialEntity
import com.example.vault.data.local.VaultNoteEntity
import com.example.vault.ui.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretVaultScreen(
    viewModel: VaultViewModel,
    uiState: com.example.vault.ui.VaultUiState
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showAddCredDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sub-Tab Switcher
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                modifier = Modifier.testTag("secret_notes_tab"),
                text = { Text("Secret Notes (${uiState.notes.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                modifier = Modifier.testTag("secret_contacts_tab"),
                text = { Text("Contacts (${uiState.contacts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                modifier = Modifier.testTag("secret_passwords_tab"),
                text = { Text("Passwords (${uiState.credentials.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedSubTab) {
                0 -> {
                    // Notes List
                    if (uiState.notes.isEmpty()) {
                        EmptyVaultState(title = "No Secret Notes", description = "Keep confidential notes, PINs, and personal records encrypted in your vault.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.notes, key = { it.id }) { note ->
                                NoteCard(note = note, onDelete = { viewModel.deleteNote(note) }, onCopy = { copyToClipboard(note.content, "Note Content") })
                            }
                        }
                    }
                }
                1 -> {
                    // Contacts List
                    if (uiState.contacts.isEmpty()) {
                        EmptyVaultState(title = "No Secret Contacts", description = "Hide confidential contacts and private phone numbers securely.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.contacts, key = { it.id }) { contact ->
                                ContactCard(contact = contact, onDelete = { viewModel.deleteContact(contact) }, onCopy = { copyToClipboard(contact.phoneNumber, "Phone Number") })
                            }
                        }
                    }
                }
                2 -> {
                    // Passwords List
                    if (uiState.credentials.isEmpty()) {
                        EmptyVaultState(title = "No Saved Passwords", description = "Store website logins, banking credentials, and account passwords safely.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.credentials, key = { it.id }) { cred ->
                                CredentialCard(cred = cred, onDelete = { viewModel.deleteCredential(cred) }, onCopyPassword = { copyToClipboard(cred.passwordEncrypted, "Password") })
                            }
                        }
                    }
                }
            }

            // FAB for Adding Content
            FloatingActionButton(
                onClick = {
                    when (selectedSubTab) {
                        0 -> showAddNoteDialog = true
                        1 -> showAddContactDialog = true
                        2 -> showAddCredDialog = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_vault_item_fab"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vault Item")
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Personal") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("New Secret Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth().testTag("note_title_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Note Content") },
                        modifier = Modifier.fillMaxWidth().height(120.dp).testTag("note_content_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNote(title, content, category)
                        showAddNoteDialog = false
                    },
                    modifier = Modifier.testTag("save_note_button")
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = { TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") } }
        )
    }

    // Add Contact Dialog
    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("New Secret Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth().testTag("contact_name_input"))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth().testTag("contact_phone_input"))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Optional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addContact(name, phone, email, notes)
                        showAddContactDialog = false
                    },
                    modifier = Modifier.testTag("save_contact_button")
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = { TextButton(onClick = { showAddContactDialog = false }) { Text("Cancel") } }
        )
    }

    // Add Credential Dialog
    if (showAddCredDialog) {
        var service by remember { mutableStateOf("") }
        var user by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCredDialog = false },
            title = { Text("New Password Credential") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = service, onValueChange = { service = it }, label = { Text("Service / Website") }, modifier = Modifier.fillMaxWidth().testTag("cred_service_input"))
                    OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username / Email") }, modifier = Modifier.fillMaxWidth().testTag("cred_user_input"))
                    OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth().testTag("cred_pass_input"))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCredential(service, user, pass, "")
                        showAddCredDialog = false
                    },
                    modifier = Modifier.testTag("save_cred_button")
                ) {
                    Text("Save Credential")
                }
            },
            dismissButton = { TextButton(onClick = { showAddCredDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EmptyVaultState(title: String, description: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoteCard(note: VaultNoteEntity, onDelete: () -> Unit, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(note.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ContactCard(contact: VaultContactEntity, onDelete: () -> Unit, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(contact.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCopy) { Icon(Icons.Default.ContentCopy, contentDescription = "Copy") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun CredentialCard(cred: VaultCredentialEntity, onDelete: () -> Unit, onCopyPassword: () -> Unit) {
    var showPassword by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(cred.serviceName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("User: ${cred.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (showPassword) cred.passwordEncrypted else "••••••••••••",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(24.dp)) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle", modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onCopyPassword, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Pass", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
