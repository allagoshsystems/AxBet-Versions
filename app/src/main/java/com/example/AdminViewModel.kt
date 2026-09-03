package com.example

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _allUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allUsers: StateFlow<List<Map<String, Any>>> = _allUsers

    private val _allDeposits = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allDeposits: StateFlow<List<Map<String, Any>>> = _allDeposits

    private val _allWithdrawals = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allWithdrawals: StateFlow<List<Map<String, Any>>> = _allWithdrawals

    private val _allBets = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allBets: StateFlow<List<Map<String, Any>>> = _allBets

    private val _adminError = MutableStateFlow<String?>(null)
    val adminError: StateFlow<String?> = _adminError

    fun loadAdminData() {
        // Load all users
        db.collection("users").get().addOnSuccessListener { snapshot ->
            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }
            _allUsers.value = users
            
            // For each user, load their transactions
            val deposits = mutableListOf<Map<String, Any>>()
            val withdrawals = mutableListOf<Map<String, Any>>()
            val bets = mutableListOf<Map<String, Any>>()
            
            users.forEach { user ->
                val uid = user["id"] as String
                db.collection("users").document(uid).collection("transactions").get().addOnSuccessListener { txSnapshot ->
                    txSnapshot.documents.forEach { txDoc ->
                        val tx = txDoc.data?.toMutableMap() ?: return@forEach
                        tx["id"] = txDoc.id
                        tx["userId"] = uid
                        tx["userEmail"] = user["email"] ?: "Unknown"
                        when (tx["type"]) {
                            "DEPOSIT" -> deposits.add(tx)
                            "WITHDRAW" -> withdrawals.add(tx)
                        }
                    }
                    _allDeposits.value = deposits.toList()
                    _allWithdrawals.value = withdrawals.toList()
                }
                
                db.collection("users").document(uid).collection("bets").get().addOnSuccessListener { betSnapshot ->
                    betSnapshot.documents.forEach { betDoc ->
                        val bet = betDoc.data?.toMutableMap() ?: return@forEach
                        bet["id"] = betDoc.id
                        bet["userId"] = uid
                        bet["userEmail"] = user["email"] ?: "Unknown"
                        bets.add(bet)
                    }
                    _allBets.value = bets.toList()
                }
            }
        }.addOnFailureListener {
            _adminError.value = "Failed to load admin data: ${it.message}"
        }
    }

    fun updateTransactionStatus(userId: String, txId: String, newStatus: String, amount: Double = 0.0, isDeposit: Boolean) {
        db.collection("users").document(userId).collection("transactions").document(txId)
            .update("status", newStatus)
            .addOnSuccessListener {
                if (newStatus == "Approved" && isDeposit) {
                    db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                        val currentBalance = doc.getDouble("balance") ?: 0.0
                        db.collection("users").document(userId).update("balance", currentBalance + amount)
                    }
                }
                loadAdminData()
            }
    }
    
    fun sendNotification(userId: String, title: String, description: String) {
        val notif = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to System.currentTimeMillis(),
            "read" to false
        )
        db.collection("users").document(userId).collection("notifications").add(notif)
            .addOnFailureListener { _adminError.value = "Failed to send notification" }
    }
}
