package com.example

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchFeed(
    val source: String,
    val sport: String,
    val last_updated: String,
    val total_matches: Int,
    val live_matches: Int,
    val upcoming_matches: Int,
    val matches: List<Match>
)

@JsonClass(generateAdapter = true)
data class Match(
    val id: String,
    val sport: String,
    val tournament: String,
    val country: String,
    val match: String,
    val teams: Teams,
    val stage: String,
    val status: String,
    val state_info: String?,
    val score: Score?,
    val url: String?,
    val odds: Odds?,
    val recent_balls: List<String>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class Teams(
    val team1: String,
    val team2: String
)

@JsonClass(generateAdapter = true)
data class Score(
    val team1_score: String?,
    val team2_score: String?
)

@JsonClass(generateAdapter = true)
data class Odds(
    val match_winner: MatchWinner?,
    val additional_markets: List<Market>? = null
)

@JsonClass(generateAdapter = true)
data class Market(
    val selection: String,
    val odds: Double
)

@JsonClass(generateAdapter = true)
data class MatchWinner(
    val team1_odds: Double?,
    val team2_odds: Double?,
    val draw_odds: Double?
)

// UI models
data class UiMatch(
    val id: String,
    val title: String,
    val team1: String,
    val team2: String,
    val tournament: String,
    val isLive: Boolean,
    val score1: String,
    val score2: String,
    val stateInfo: String,
    val stage: String,
    val odds1: Double?,
    val odds2: Double?,
    val additionalMarkets: List<Market> = emptyList(),
    val recentBalls: List<String> = emptyList()
)

data class Transaction(
    val id: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val status: String = "Pending",
    val date: Long = 0L,
    val details: String = ""
)

data class Bet(
    val id: String = "",
    val matchTitle: String = "",
    val team: String = "",
    val amount: Double = 0.0,
    val odds: Double = 0.0,
    val status: String = "Pending",
    val date: Long = 0L
)

data class Notification(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val read: Boolean = false
)
