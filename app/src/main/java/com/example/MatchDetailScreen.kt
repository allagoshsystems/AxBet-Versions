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
            
            if (match.isLive && match.recentBalls.isNotEmpty()) {
                RecentOversGrid(balls = match.recentBalls)
            }
            
            // Odds Section
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Match Winner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Additional Markets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        match.additionalMarkets.chunked(2).forEach { rowMarkets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                // If odd number of items, add an empty spacer for the remaining weight
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trend > 0) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Up", tint = Color.Green, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                } else if (trend < 0) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Down", tint = Color.Red, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                }
                
                val oddsColor = when {
                    trend > 0 -> Color.Green
                    trend < 0 -> Color.Red
                    else -> MaterialTheme.colorScheme.primary
                }
                
                Text(
                    text = odds.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = oddsColor
                )
            }
        }
    }
}

@Composable
fun RecentOversGrid(balls: List<String>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Previous 2 Overs", style = MaterialTheme.typography.titleSmall, color = Color.LightGray)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show exactly 2 overs (12 balls) in a grid to avoid scrolling
        // We'll create a simple Row with wrapping or two rows to ensure it fits perfectly.
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val over1 = balls.take(6)
            val over2 = if (balls.size > 6) balls.drop(6).take(6) else emptyList()
            
            if (over1.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (ball in over1) {
                        BallIcon(ball)
                    }
                    // Pad if less than 6 balls
                    for (i in 0 until (6 - over1.size)) {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }
            }
            if (over2.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (ball in over2) {
                        BallIcon(ball)
                    }
                    for (i in 0 until (6 - over2.size)) {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BallIcon(ball: String) {
    val b = ball.lowercase()
    val (bgColor, textColor) = when {
        b == "4" -> Pair(Color(0xFF4CAF50), Color.White) // Green
        b == "6" -> Pair(Color(0xFF9C27B0), Color.White) // Purple
        b == "w" -> Pair(Color(0xFFF44336), Color.White) // Red wicket
        b in listOf("wd", "nb", "lb", "1lb", "b") -> Pair(Color(0xFFFFEB3B), Color.Black) // Yellow for extras
        else -> Pair(Color.White, Color.Black) // White for 1, 2, 0, 3 etc
    }
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ball.uppercase(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
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
        
        kotlinx.coroutines.delay(5000)
        trend = 0
    }
    
    OddsButton(teamName = teamName, odds = odds, trend = trend, modifier = modifier, onClick = onClick)
}

@Composable
fun ScoreDisplay(score: String) {
    if (score.isEmpty()) return
    // Parimatch score string is often like "125/4 (15.2)" or similar.
    // If it contains a slash, try to parse Runs and Wickets.
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