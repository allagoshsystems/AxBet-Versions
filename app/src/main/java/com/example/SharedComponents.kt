package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BetDialog(
    team: String,
    odds: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Place Bet on $team") },
        text = {
            Column {
                Text("Odds: $odds")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Bet Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (amountStr.toDoubleOrNull() != null) {
                    Text("Potential Return: PKR ${"%.2f".format(amountStr.toDouble() * odds)}", modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                val amt = amountStr.toDoubleOrNull() ?: 0.0
                onConfirm(amt)
            }) {
                Text("Place Bet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
