import sys

content = """package com.example

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthViewModel {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<com.google.firebase.auth.FirebaseUser?> = _user.asStateFlow()

    private val _userProfile = MutableStateFlow<Map<String, Any>?>(null)
    val userProfile: StateFlow<Map<String, Any>?> = _userProfile.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _bets = MutableStateFlow<List<Bet>>(emptyList())
    val bets: StateFlow<List<Bet>> = _bets.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
            if (firebaseAuth.currentUser != null) {
                val uid = firebaseAuth.currentUser!!.uid
                fetchUserProfile(uid)
                fetchHistory(uid)
            } else {
                _userProfile.value = null
                _transactions.value = emptyList()
                _bets.value = emptyList()
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun setError(msg: String?) {
        _error.value = msg
    }

    private fun fetchUserProfile(uid: String) {
        db.collection("users").document(uid).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                _userProfile.value = snapshot.data
            }
        }
    }
    
    private fun fetchHistory(uid: String) {
        db.collection("users").document(uid).collection("transactions")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    _transactions.value = snapshot.documents.mapNotNull {
                        Transaction(
                            id = it.id,
                            type = it.getString("type") ?: "",
                            amount = it.getDouble("amount") ?: 0.0,
                            status = it.getString("status") ?: "Pending",
                            date = it.getLong("date") ?: 0L,
                            details = it.getString("details") ?: ""
                        )
                    }.sortedByDescending { it.date }
                }
            }

        db.collection("users").document(uid).collection("bets")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    _bets.value = snapshot.documents.mapNotNull {
                        Bet(
                            id = it.id,
                            matchTitle = it.getString("matchTitle") ?: "",
                            team = it.getString("team") ?: "",
                            amount = it.getDouble("amount") ?: 0.0,
                            odds = it.getDouble("odds") ?: 0.0,
                            status = it.getString("status") ?: "Pending",
                            date = it.getLong("date") ?: 0L
                        )
                    }.sortedByDescending { it.date }
                }
            }

        db.collection("users").document(uid).collection("notifications")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    _notifications.value = snapshot.documents.mapNotNull {
                        Notification(
                            id = it.id,
                            title = it.getString("title") ?: "",
                            description = it.getString("description") ?: "",
                            date = it.getLong("date") ?: 0L,
                            read = it.getBoolean("read") ?: false
                        )
                    }.sortedByDescending { it.date }
                }
            }
    }
    
    fun requestWithdraw(amount: Double, name: String, accountNum: String) {
        val uid = auth.currentUser?.uid ?: return
        val currentBalance = _userProfile.value?.get("balance")?.toString()?.toDoubleOrNull() ?: 0.0
        
        if (amount <= 0) {
            _error.value = "Enter a valid amount"
            return
        }
        if (amount > currentBalance) {
            _error.value = "Insufficient balance"
            return
        }
        val newBalance = currentBalance - amount
        db.collection("users").document(uid).update("balance", newBalance)
        
        val trx = hashMapOf(
            "type" to "WITHDRAW",
            "amount" to amount,
            "status" to "Pending",
            "date" to System.currentTimeMillis(),
            "details" to "Name: $name, Acct: $accountNum"
        )
        db.collection("users").document(uid).collection("transactions").add(trx)
    }

    fun requestDeposit(amount: Double, bankName: String, trxId: String) {
        val uid = auth.currentUser?.uid ?: return
        if (amount <= 0) {
            _error.value = "Enter a valid amount"
            return
        }
        if (trxId.length != 11 || !trxId.all { it.isDigit() }) {
            _error.value = "invalid transaction id"
            return
        }
        
        val trx = hashMapOf(
            "type" to "DEPOSIT",
            "amount" to amount,
            "status" to "Pending",
            "date" to System.currentTimeMillis(),
            "details" to "Bank: $bankName, TrxID: $trxId"
        )
        db.collection("users").document(uid).collection("transactions").add(trx)
    }

    fun placeBet(matchTitle: String, team: String, amount: Double, odds: Double) {
        val uid = auth.currentUser?.uid ?: return
        val currentBalance = _userProfile.value?.get("balance")?.toString()?.toDoubleOrNull() ?: 0.0
        
        if (amount <= 0) {
            _error.value = "Enter a valid bet amount"
            return
        }
        if (amount > currentBalance) {
            _error.value = "Insufficient balance"
            return
        }
        
        val newBalance = currentBalance - amount
        db.collection("users").document(uid).update("balance", newBalance)
        
        val bet = hashMapOf(
            "matchTitle" to matchTitle,
            "team" to team,
            "amount" to amount,
            "odds" to odds,
            "status" to "Pending",
            "date" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).collection("bets").add(bet)
    }

    suspend fun signIn(email: String, pass: String) {
        _isLoading.value = true
        _error.value = null
        try {
            auth.signInWithEmailAndPassword(email, pass).await()
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Sign in failed"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun register(email: String, pass: String, fullName: String, phone: String) {
        _isLoading.value = true
        _error.value = null
        try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid
            if (uid != null) {
                val profile = hashMapOf(
                    "full_name" to fullName,
                    "phone" to phone,
                    "balance" to 0.0,
                    "email" to email
                )
                db.collection("users").document(uid).set(profile).await()
            }
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Registration failed"
        } finally {
            _isLoading.value = false
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
"""

with open("/app/applet/app/src/main/java/com/example/AuthViewModel.kt", "w") as f:
    f.write(content)
print("Reverted AuthViewModel.kt")
