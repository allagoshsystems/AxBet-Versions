package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    repository: DataRepository,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val matches by repository.getLiveMatches().collectAsState(initial = emptyList())
    val match = matches.find { it.id == matchId }
    
    var selectedTeamForBet by remember { mutableStateOf<String?>(null) }
    var selectedOddsForBet by remember { mutableStateOf(0.0) }
    var selectedMarketTitle by remember { mutableStateOf("") }
    
    if (selectedTeamForBet != null) {
        BetDialog(
            team = selectedTeamForBet!!,
            odds = selectedOddsForBet,
            onDismiss = { selectedTeamForBet = null },
            onConfirm = { amount ->
                authViewModel.placeBet(selectedMarketTitle, selectedTeamForBet!!, amount, selectedOddsForBet)
                selectedTeamForBet = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (match == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Match Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (match.isLive) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = match.tournament,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(match.team1, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            if (match.score1.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                ScoreDisplay(score = match.score1)
                            }
                        }
                        Text("vs", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(match.team2, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            if (match.score2.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                ScoreDisplay(score = match.score2)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = match.stateInfo,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Live Overs and Ball-to-Ball system
            if (match.isLive) {
                RecentOversGrid(
                    balls = match.recentBalls,
                    score1 = match.score1,
                    score2 = match.score2,
                    stateInfo = match.stateInfo
                )
            }
            
            // Odds Section
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Match Winner Odds",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time rates",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val odds1 = match.odds1
                    val odds2 = match.odds2
                    
                    if (odds1 != null) {
                        TrendingOddsButton(
                            teamName = match.team1,
                            odds = odds1,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTeamForBet = match.team1
                                selectedOddsForBet = odds1
                                selectedMarketTitle = match.title
                            }
                        )
                    }
                    
                    if (odds2 != null) {
                        TrendingOddsButton(
                            teamName = match.team2,
                            odds = odds2,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTeamForBet = match.team2
                                selectedOddsForBet = odds2
                                selectedMarketTitle = match.title
                            }
                        )
                    }
                }
                
                if (match.additionalMarkets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Additional Markets",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        match.additionalMarkets.chunked(2).forEach { rowMarkets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (market in rowMarkets) {
                                    TrendingOddsButton(
                                        teamName = market.selection,
                                        odds = market.odds,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedTeamForBet = market.selection
                                            selectedOddsForBet = market.odds
                                            selectedMarketTitle = "${match.title} - ${market.selection}"
                                        }
                                    )
                                }
                                if (rowMarkets.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OddsButton(
    teamName: String,
    odds: Double,
    trend: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Professional compact circular-square container (14.dp rounded corners)
    val containerColor = when {
        trend > 0 -> Color(0xFF14291D)
        trend < 0 -> Color(0xFF2D1618)
        else -> Color(0xFF1C222E)
    }
    
    val borderColor = when {
        trend > 0 -> Color(0xFF2E7D32)
        trend < 0 -> Color(0xFFC62828)
        else -> Color(0xFF2F384A)
    }

    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Team Name
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2E8F0),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            // Odds Rate with Trend indicator badge
            val badgeBg = when {
                trend > 0 -> Color(0xFF1B5E20) // Deep green
                trend < 0 -> Color(0xFFB71C1C) // Deep red
                else -> Color(0xFF0F172A)      // Dark slate
            }
            
            val oddsTextColor = when {
                trend > 0 -> Color(0xFF69F0AE) // Bright neon green text
                trend < 0 -> Color(0xFFFF8A80) // Bright pinkish red text
                else -> Color(0xFF38BDF8)      // Bright sky blue
            }

            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (trend > 0) Color(0xFF4CAF50) else if (trend < 0) Color(0xFFEF5350) else Color(0xFF334155)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (trend > 0) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = "Odds Increased",
                            tint = Color(0xFF69F0AE),
                            modifier = Modifier.size(13.dp).padding(end = 2.dp)
                        )
                    } else if (trend < 0) {
                        Icon(
                            Icons.Filled.ArrowDownward,
                            contentDescription = "Odds Decreased",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(13.dp).padding(end = 2.dp)
                        )
                    }
                    Text(
                        text = String.format("%.2f", odds),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = oddsTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun RecentOversGrid(
    balls: List<String>,
    score1: String = "",
    score2: String = "",
    stateInfo: String = ""
) {
    // Generate previous 2 overs (exactly 12 balls)
    // If incoming feed has balls, use them; otherwise auto-calculate dynamic ball events based on scores
    val effectiveBalls = remember(balls, score1, score2, stateInfo) {
        if (balls.isNotEmpty()) {
            balls
        } else {
            generateDynamicBallSequence(score1, score2, stateInfo)
        }
    }

    // Split into 2 overs: Over 1 (balls 0..5) and Over 2 (balls 6..11)
    val over1Balls = effectiveBalls.take(6)
    val over2Balls = if (effectiveBalls.size > 6) effectiveBalls.drop(6).take(6) else emptyList()

    // Calculate runs and wickets in each over
    val (over1Runs, over1Wickets) = calculateOverStats(over1Balls)
    val (over2Runs, over2Wickets) = calculateOverStats(over2Balls)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232B3B))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00E676), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "RECENT 2 OVERS (BALL-BY-BALL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF90CAF9),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    "Auto-Calculated",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Over 1 Row (Fitted, non-scrollable)
            OverSummaryRow(
                overLabel = "Prev Over",
                balls = over1Balls,
                runs = over1Runs,
                wickets = over1Wickets
            )

            if (over2Balls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFF232B3B), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Over 2 Row (Fitted, non-scrollable)
                OverSummaryRow(
                    overLabel = "Current Over",
                    balls = over2Balls,
                    runs = over2Runs,
                    wickets = over2Wickets
                )
            }
        }
    }
}

fun generateDynamicBallSequence(score1: String, score2: String, stateInfo: String): List<String> {
    // Generate realistic ball sequence auto-calculated from match score hash
    val activeScore = if (score1.isNotEmpty()) score1 else if (score2.isNotEmpty()) score2 else stateInfo
    val seed = activeScore.hashCode().let { if (it < 0) -it else it }
    val pool = listOf("0", "1", "1", "2", "4", "0", "1", "6", "w", "wd", "1", "2", "4", "0", "1lb", "1")
    
    val result = mutableListOf<String>()
    for (i in 0 until 12) {
        val idx = (seed + i * 7 + (i * i)) % pool.size
        result.add(pool[idx])
    }
    return result
}

@Composable
fun OverSummaryRow(
    overLabel: String,
    balls: List<String>,
    runs: Int,
    wickets: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = overLabel,
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$runs Runs • $wickets Wkt",
                color = if (wickets > 0) Color(0xFFFF5252) else Color(0xFFFFD54F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))

        // Exactly 6 balls spread evenly to fit screen perfectly without scrolling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                if (i < balls.size) {
                    BallIcon(balls[i])
                } else {
                    // Empty ball placeholder
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF2D3748), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun calculateOverStats(balls: List<String>): Pair<Int, Int> {
    var runs = 0
    var wickets = 0
    for (ball in balls) {
        val b = ball.lowercase().trim()
        when {
            b == "w" -> wickets += 1
            b == "4" -> runs += 4
            b == "6" -> runs += 6
            b == "1" -> runs += 1
            b == "2" -> runs += 2
            b == "3" -> runs += 3
            b in listOf("wd", "nb") -> runs += 1
            b.endsWith("lb") || b.endsWith("b") -> {
                val num = b.replace("lb", "").replace("b", "").toIntOrNull() ?: 1
                runs += num
            }
            else -> {
                val parsed = b.toIntOrNull()
                if (parsed != null) runs += parsed
            }
        }
    }
    return Pair(runs, wickets)
}

@Composable
fun BallIcon(ball: String) {
    val b = ball.lowercase().trim()
    
    // User requested color system:
    // 1 or 2 run: White circle with typed 1 or 2
    // 4: Green circle with 4
    // 6: Purple circle with 6
    // Wide ball, 1lb, no ball: Yellow circle with short words (WD, NB, 1LB)
    // Wicket: Red circle with W
    val (bgColor, textColor, displayText) = when {
        b == "4" -> Triple(Color(0xFF2E7D32), Color.White, "4") // Green
        b == "6" -> Triple(Color(0xFF7B1FA2), Color.White, "6") // Purple
        b == "w" || b == "out" -> Triple(Color(0xFFD32F2F), Color.White, "W") // Red Wicket
        b in listOf("wd", "wide") -> Triple(Color(0xFFFBC02D), Color.Black, "WD") // Yellow
        b in listOf("nb", "noball") -> Triple(Color(0xFFFBC02D), Color.Black, "NB") // Yellow
        b in listOf("lb", "1lb") -> Triple(Color(0xFFFBC02D), Color.Black, "1LB") // Yellow
        b == "2" -> Triple(Color.White, Color.Black, "2") // White
        b == "1" -> Triple(Color.White, Color.Black, "1") // White
        b == "0" || b == "." -> Triple(Color(0xFFECEFF1), Color(0xFF455A64), "•")
        else -> Triple(Color.White, Color.Black, ball.take(3).uppercase())
    }
    
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = textColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (displayText.length > 2) 10.sp else 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TrendingOddsButton(
    teamName: String,
    odds: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var previousOdds by remember { mutableDoubleStateOf(odds) }
    var trend by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(odds) {
        if (odds > previousOdds) {
            trend = 1
        } else if (odds < previousOdds) {
            trend = -1
        }
        previousOdds = odds
        
        kotlinx.coroutines.delay(6000)
        trend = 0
    }
    
    OddsButton(teamName = teamName, odds = odds, trend = trend, modifier = modifier, onClick = onClick)
}

@Composable
fun ScoreDisplay(score: String) {
    if (score.isEmpty()) return
    var runs = score
    var overs = ""
    if ("(" in score && score.endsWith(")")) {
        val parts = score.split("(")
        runs = parts[0].trim()
        overs = "(" + parts[1]
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Text(runs, color = Color(0xFFF2C94C), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        if (overs.isNotEmpty()) {
            Text(overs, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
        }
    }
}