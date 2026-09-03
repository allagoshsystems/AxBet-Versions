package com.example

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

class DataRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    fun getLiveMatches(): Flow<List<UiMatch>> = callbackFlow {
        var latestFeed: MatchFeed? = null
        var hiddenMatches: List<String> = emptyList()
        var hiddenOdds: List<String> = emptyList()

        fun updateUI() {
            val uiMatches = latestFeed?.matches?.filter { match ->
                !hiddenMatches.contains(match.id)
            }?.map { match ->
                val isOddsHidden = hiddenOdds.contains(match.id)
                UiMatch(
                    id = match.id,
                    title = match.match,
                    team1 = match.teams.team1,
                    team2 = match.teams.team2,
                    tournament = match.tournament,
                    isLive = match.stage == "live",
                    score1 = match.score?.team1_score ?: "",
                    score2 = match.score?.team2_score ?: "",
                    stateInfo = match.state_info ?: "",
                    stage = match.stage,
                    odds1 = if (isOddsHidden) null else match.odds?.match_winner?.team1_odds,
                    odds2 = if (isOddsHidden) null else match.odds?.match_winner?.team2_odds,
                    additionalMarkets = if (isOddsHidden) emptyList() else match.odds?.additional_markets ?: emptyList()
                )
            } ?: emptyList()
            trySend(uiMatches)
        }

        val feedListener = db.collection("cricket_odds").document("live_feed")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val data = snapshot.data
                        if (data != null) {
                            val jsonString = JSONObject(data as Map<*, *>).toString()
                            val adapter = moshi.adapter(MatchFeed::class.java)
                            latestFeed = adapter.fromJson(jsonString)
                            updateUI()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        val adminListener = db.collection("cricket_odds").document("admin_controls")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val hiddenM = snapshot.get("hiddenMatches") as? List<*>
                        hiddenMatches = hiddenM?.filterIsInstance<String>() ?: emptyList()
                        
                        val hiddenO = snapshot.get("hiddenOdds") as? List<*>
                        hiddenOdds = hiddenO?.filterIsInstance<String>() ?: emptyList()
                        
                        updateUI()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        awaitClose { 
            feedListener.remove()
            adminListener.remove()
        }
    }
}
