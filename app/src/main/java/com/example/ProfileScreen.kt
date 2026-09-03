package com.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusText(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> Color(0xFFFFEB3B)
        "reject", "lost" -> Color(0xFFDE1A1A)
        "cancel", "cancelled" -> Color.White
        "success", "won" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(text = status.uppercase(), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun ProfileScreen(authViewModel: AuthViewModel, onNavigateToAdmin: () -> Unit = {}) {
    val user by authViewModel.user.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val transactions by authViewModel.transactions.collectAsState()
    val bets by authViewModel.bets.collectAsState()
    
    val balance = userProfile?.get("balance")?.toString()?.toDoubleOrNull() ?: 0.0
    val fullName = userProfile?.get("full_name")?.toString() ?: user?.email?.substringBefore("@") ?: "User"

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Deposits", "Withdrawals", "Bets")
    
    var showDeposit by remember { mutableStateOf(false) }
    var showWithdraw by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Welcome, $fullName", style = MaterialTheme.typography.headlineMedium)
            Text(text = user?.email ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Available Balance", style = MaterialTheme.typography.bodyMedium)
                    Text("PKR ${"%.2f".format(balance)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showDeposit = true }, modifier = Modifier.weight(1f)) {
                            Text("Deposit")
                        }
                        OutlinedButton(onClick = { showWithdraw = true }, modifier = Modifier.weight(1f)) {
                            Text("Withdraw")
                        }
                    }
                }
            }
        }
        
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(text = title, maxLines = 1, softWrap = false) })
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            when (selectedTab) {
                0 -> { // Overview
                    item {
                        ListItem(
                            headlineContent = { Text("Privacy Policy") },
                            leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                            modifier = Modifier.clickable { showPrivacy = true }
                        )
                        ListItem(
                            headlineContent = { Text("Settings") },
                            leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            modifier = Modifier.clickable { showInfo = "Settings available soon." }
                        )
                        ListItem(
                            headlineContent = { Text("Start Live Chat") },
                            leadingContent = { Icon(Icons.Filled.Chat, contentDescription = null) },
                            modifier = Modifier.clickable { showInfo = "Live chat agents are currently offline." }
                        )
                        ListItem(
                            headlineContent = { Text("Create Ticket") },
                            leadingContent = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = null) },
                            modifier = Modifier.clickable { showInfo = "Ticket creation system will be online shortly." }
                        )
                        
                        if (user?.email.equals("fajr.ent@gmail.com", ignoreCase = true)) {
                            ListItem(
                                headlineContent = { Text("Admin Panel") },
                                leadingContent = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                                modifier = Modifier.clickable { onNavigateToAdmin() },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    headlineColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    leadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { authViewModel.signOut() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Sign Out")
                        }
                    }
                }
                1 -> { // Deposits
                    val deps = transactions.filter { it.type == "DEPOSIT" }
                    if (deps.isEmpty()) item { Text("No deposits found.", modifier = Modifier.padding(16.dp)) }
                    items(deps) { trx -> TransactionCard(trx) }
                }
                2 -> { // Withdrawals
                    val withs = transactions.filter { it.type == "WITHDRAW" }
                    if (withs.isEmpty()) item { Text("No withdrawals found.", modifier = Modifier.padding(16.dp)) }
                    items(withs) { trx -> TransactionCard(trx) }
                }
                3 -> { // Bets
                    if (bets.isEmpty()) item { Text("No bets found.", modifier = Modifier.padding(16.dp)) }
                    items(bets) { bet -> BetCard(bet) }
                }
            }
        }
    }
    
    // Dialogs
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("Privacy Policy") },
            text = { Text("AxBet values your privacy. All your data, including transactions and bets, is encrypted and securely stored. We do not share your information with third parties.") },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("Close") } }
        )
    }
    if (showInfo != null) {
        AlertDialog(
            onDismissRequest = { showInfo = null },
            title = { Text("Information") },
            text = { Text(showInfo!!) },
            confirmButton = { TextButton(onClick = { showInfo = null }) { Text("OK") } }
        )
    }
    if (showDeposit) {
        DepositDialog(authViewModel) { showDeposit = false }
    }
    if (showWithdraw) {
        WithdrawDialog(authViewModel) { showWithdraw = false }
    }
}

@Composable
fun DepositDialog(authViewModel: AuthViewModel, onDismiss: () -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("Nayapay") }
    var trxId by remember { mutableStateOf("") }
    val banks = listOf("Nayapay", "SadaPay", "Upaisa", "Easypaisa")
    
    val accountInfo = when (selectedBank) {
        "Nayapay" -> "03359405954"
        "SadaPay" -> "03359405954"
        "Upaisa" -> "03359405954"
        "Easypaisa" -> "03439881669"
        else -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deposit Funds") },
        text = {
            Column {
                Text("Select Bank:", fontWeight = FontWeight.Bold)
                banks.forEach { bank ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedBank = bank }) {
                        RadioButton(selected = selectedBank == bank, onClick = { selectedBank = bank })
                        Text(bank)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("PROMPT:\nPlease transfer your funds to the account details below, then enter your deposited amount and Transaction ID to create a request.\n\nAccount: $accountInfo\nTitle: Rakhshanda Jabeen\nBank: $selectedBank", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount Deposited") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = trxId,
                    onValueChange = { trxId = it },
                    label = { Text("Transaction ID (11 Digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                authViewModel.requestDeposit(amountStr.toDoubleOrNull() ?: 0.0, selectedBank, trxId)
                onDismiss()
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun WithdrawDialog(authViewModel: AuthViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Funds") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Enter Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = account, onValueChange = { account = it }, label = { Text("Account Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Amount to Withdraw") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                authViewModel.requestWithdraw(amountStr.toDoubleOrNull() ?: 0.0, name, account)
                onDismiss() // Error handling via global alerter
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun TransactionCard(trx: Transaction) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(trx.type, fontWeight = FontWeight.Bold)
                Text(trx.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(sdf.format(Date(trx.date)), style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("PKR ${"%.2f".format(trx.amount)}", fontWeight = FontWeight.Bold)
                StatusText(trx.status)
            }
        }
    }
}

@Composable
fun BetCard(bet: Bet) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(bet.matchTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Selected: ${bet.team} (@${bet.odds})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(sdf.format(Date(bet.date)), style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("PKR ${"%.2f".format(bet.amount)}", fontWeight = FontWeight.Bold)
                StatusText(bet.status)
            }
        }
    }
}
