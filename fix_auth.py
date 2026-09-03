import sys
with open("/app/applet/app/src/main/java/com/example/AuthViewModel.kt", "r") as f:
    content = f.read()

replacement = """    private fun fetchHistory(uid: String) {
        db.collection("transactions")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    _transactions.value = snapshot.documents.mapNotNull {
                        Transaction(
                            id = it.id,
                            type = it.getString("type") ?: "",
                            amount = it.getDouble("amount") ?: 0.0,
                            status = it.getString("status") ?: "pending",
                            date = it.getLong("date") ?: 0L,
                            details = it.getString("details") ?: ""
                        )
                    }.sortedByDescending { it.date }
                }
            }

        db.collection("bets")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    _bets.value = snapshot.documents.mapNotNull {
                        Bet(
                            id = it.id,
                            matchTitle = it.getString("matchTitle") ?: "",
                            team = it.getString("team") ?: "",
                            amount = it.getDouble("amount") ?: 0.0,
                            odds = it.getDouble("odds") ?: 0.0,
                            status = it.getString("status") ?: "pending",
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
        val user = auth.currentUser ?: return
        val uid = user.uid
        val email = user.email ?: ""
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
            "userId" to uid,
            "userEmail" to email,
            "type" to "withdrawal",
            "amount" to amount,
            "status" to "pending",
            "date" to System.currentTimeMillis(),
            "details" to "Name: $name, Acct: $accountNum",
            "paymentDetails" to "Name: $name, Acct: $accountNum"
        )
        db.collection("transactions").add(trx)
    }

    fun requestDeposit(amount: Double, bankName: String, trxId: String) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val email = user.email ?: ""
        if (amount <= 0) {
            _error.value = "Enter a valid amount"
            return
        }
        if (trxId.length != 11 || !trxId.all { it.isDigit() }) {
            _error.value = "invalid transaction id"
            return
        }
        
        val trx = hashMapOf(
            "userId" to uid,
            "userEmail" to email,
            "type" to "deposit",
            "amount" to amount,
            "status" to "pending",
            "date" to System.currentTimeMillis(),
            "details" to "Bank: $bankName, TrxID: $trxId"
        )
        db.collection("transactions").add(trx)
    }

    fun placeBet(matchTitle: String, team: String, amount: Double, odds: Double) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val email = user.email ?: ""
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
            "userId" to uid,
            "userEmail" to email,
            "matchTitle" to matchTitle,
            "team" to team,
            "amount" to amount,
            "odds" to odds,
            "status" to "pending",
            "date" to System.currentTimeMillis()
        )
        db.collection("bets").add(bet)
    }"""

import re
pattern = re.compile(r'    private fun fetchHistory\(uid: String\) \{.*db\.collection\("users"\)\.document\(uid\)\.collection\("bets"\)\.add\(bet\)\n    \}', re.DOTALL)
new_content = pattern.sub(replacement, content)

with open("/app/applet/app/src/main/java/com/example/AuthViewModel.kt", "w") as f:
    f.write(new_content)
print("Updated AuthViewModel.kt")
