package com.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit, adminViewModel: AdminViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        adminViewModel.loadAdminData()
    }

    val users by adminViewModel.allUsers.collectAsState()
    val deposits by adminViewModel.allDeposits.collectAsState()
    val withdrawals by adminViewModel.allWithdrawals.collectAsState()
    val bets by adminViewModel.allBets.collectAsState()
    
    var selectedTab by remember { mutableStateOf("Users") }
    val tabs = listOf("Users", "Deposits", "Withdrawals", "Bets", "OTA Updates", "Reports", "Chat/Tickets", "Match Tools")

    var selectedUserForNotif by remember { mutableStateOf<String?>(null) }
    
    if (selectedUserForNotif != null) {
        var notifTitle by remember { mutableStateOf("") }
        var notifDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedUserForNotif = null },
            title = { Text("Send Notification") },
            text = {
                Column {
                    OutlinedTextField(value = notifTitle, onValueChange = { notifTitle = it }, label = { Text("Title") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = notifDesc, onValueChange = { notifDesc = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminViewModel.sendNotification(selectedUserForNotif!!, notifTitle, notifDesc)
                    selectedUserForNotif = null
                }) { Text("Send") }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForNotif = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            ScrollableTabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab) }
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                when (selectedTab) {
                    "Users" -> UsersList(users) { uid -> selectedUserForNotif = uid }
                    "Deposits" -> TransactionsList(deposits, isDeposit = true, adminViewModel)
                    "Withdrawals" -> TransactionsList(withdrawals, isDeposit = false, adminViewModel)
                    "Bets" -> BetsListAdmin(bets)
                    "OTA Updates" -> OtaUpdateAdminPanel(adminViewModel)
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("$selectedTab feature is coming soon.")
                    }
                }
            }
        }
    }
}

@Composable
fun OtaUpdateAdminPanel(adminViewModel: AdminViewModel) {
    var versionCodeText by remember { mutableStateOf("") }
    var versionNameText by remember { mutableStateOf("") }
    var whatsNewText by remember { mutableStateOf("") }
    var apkUrlText by remember { mutableStateOf("") }
    var isMandatory by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Publish In-App (OTA) Update", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Users with an older version will automatically see the update prompt and install the new APK.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = versionCodeText,
            onValueChange = { versionCodeText = it },
            label = { Text("New Version Code (Installed: ${BuildConfig.VERSION_CODE})") },
            placeholder = { Text("${BuildConfig.VERSION_CODE + 1}") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = versionNameText,
            onValueChange = { versionNameText = it },
            label = { Text("New Version Name (Installed: ${BuildConfig.VERSION_NAME})") },
            placeholder = { Text("${BuildConfig.VERSION_NAME}.1") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apkUrlText,
            onValueChange = { apkUrlText = it },
            label = { Text("Direct APK Download URL (GitHub Releases URL)") },
            placeholder = { Text("https://github.com/.../releases/download/.../app-release.apk") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = whatsNewText,
            onValueChange = { whatsNewText = it },
            label = { Text("What's New / Changelog") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = isMandatory, onCheckedChange = { isMandatory = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mandatory update (cannot skip)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (statusMessage != null) {
            Text(
                text = statusMessage!!,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val code = versionCodeText.toIntOrNull()
                if (code == null || code <= 0) {
                    statusMessage = "Please enter a valid version code number (e.g. 2)"
                    isError = true
                    return@Button
                }
                if (apkUrlText.isBlank()) {
                    statusMessage = "Please provide the APK download URL"
                    isError = true
                    return@Button
                }

                isSubmitting = true
                adminViewModel.publishAppUpdate(
                    versionCode = code,
                    versionName = versionNameText.ifBlank { "v$code" },
                    whatsNew = whatsNewText.ifBlank { "Performance improvements and bug fixes." },
                    apkUrl = apkUrlText.trim(),
                    isMandatory = isMandatory,
                    onSuccess = {
                        isSubmitting = false
                        isError = false
                        statusMessage = "Update published! Users will receive the update dialog immediately."
                    },
                    onError = { err ->
                        isSubmitting = false
                        isError = true
                        statusMessage = err
                    }
                )
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Publishing..." else "Publish OTA Update to App")
        }
    }
}

@Composable
fun UsersList(users: List<Map<String, Any>>, onSendNotif: (String) -> Unit) {
    LazyColumn {
        items(users) { user ->
            val dateStr = user["dateJoined"]?.let {
                SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(it as Long))
            } ?: "Unknown Date"
            
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Email: ${user["email"]}", fontWeight = FontWeight.Bold)
                    Text("Balance: PKR ${user["balance"]}")
                    Text("Joined: $dateStr")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onSendNotif(user["id"] as String) }) {
                        Icon(Icons.Filled.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Notification")
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsList(transactions: List<Map<String, Any>>, isDeposit: Boolean, adminViewModel: AdminViewModel) {
    LazyColumn {
        items(transactions) { tx ->
            val status = tx["status"] as? String ?: "Pending"
            val amount = tx["amount"]?.toString()?.toDoubleOrNull() ?: 0.0
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("User: ${tx["userEmail"]}", fontWeight = FontWeight.Bold)
                    Text("Amount: PKR $amount")
                    Text("Status: $status")
                    Text("Details: ${tx["details"]}")
                    
                    if (status == "Pending") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { adminViewModel.updateTransactionStatus(tx["userId"] as String, tx["id"] as String, "Rejected", amount, isDeposit) }) {
                                Text("Reject", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { adminViewModel.updateTransactionStatus(tx["userId"] as String, tx["id"] as String, "Approved", amount, isDeposit) }) {
                                Text("Approve")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BetsListAdmin(bets: List<Map<String, Any>>) {
    LazyColumn {
        items(bets) { bet ->
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("User: ${bet["userEmail"]}", fontWeight = FontWeight.Bold)
                    Text("Match: ${bet["match"]}")
                    Text("Team: ${bet["team"]}")
                    Text("Stake: PKR ${bet["amount"]}")
                    Text("Status: ${bet["status"]}")
                }
            }
        }
    }
}
