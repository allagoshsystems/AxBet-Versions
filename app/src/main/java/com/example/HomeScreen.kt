package com.example

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiveRed
import com.example.ui.theme.UpcomingBlue
import com.example.ui.theme.CardDark
import com.example.ui.theme.BorderDark

@Composable
fun HomeScreen(repository: DataRepository, authViewModel: AuthViewModel, onNavigateToProfile: () -> Unit, onNavigateToMatch: (String) -> Unit) {
    val matches by repository.getLiveMatches().collectAsState(initial = emptyList())
    val userProfile by authViewModel.userProfile.collectAsState()
    val balance = userProfile?.get("balance")?.toString()?.toDoubleOrNull() ?: 0.0
    
    var betTeam by remember { mutableStateOf<String?>(null) }
    var betOdds by remember { mutableStateOf(0.0) }
    var betMatchTitle by remember { mutableStateOf("") }
    
    val notifications by authViewModel.notifications.collectAsState()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Notifications") },
            text = {
                if (notifications.isEmpty()) {
                    Text("No notifications yet.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                        items(notifications) { notif ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notif.description, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) { Text("Close") }
            }
        )
    }

    if (betTeam != null) {
        BetDialog(
            team = betTeam!!,
            odds = betOdds,
            onDismiss = { betTeam = null },
            onConfirm = { amt ->
                authViewModel.placeBet(betMatchTitle, betTeam!!, amt, betOdds)
                betTeam = null
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF000000), Color(0xFF152238), Color(0xFF203A43))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.axbet_logo_foreground_1788272432170),
                        contentDescription = "AxBet Logo",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AXBET",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        Box {
                            Icon(androidx.compose.material.icons.Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                            if (notifications.any { !it.read }) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Red, androidx.compose.foundation.shape.CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clickable { onNavigateToProfile() }
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFF2994A), Color(0xFFF2C94C))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PKR ${"%.2f".format(balance)}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Series & Leagues",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        item {
            val leagues = listOf(
                R.drawable.banner_psl_text_1788277882254,
                R.drawable.banner_ipl_text_1788277899010,
                R.drawable.banner_bbl_text_1788277915101,
                R.drawable.banner_hundred_text_1788277934398,
                R.drawable.banner_cpl_text_1788277948518,
                R.drawable.banner_topend_text_1788277967164
            )
            
            val startIndex = Int.MAX_VALUE / 2
            val initialPage = startIndex - (startIndex % leagues.size)
            val pagerState = androidx.compose.foundation.pager.rememberPagerState(
                initialPage = initialPage,
                pageCount = { Int.MAX_VALUE }
            )
            
            LaunchedEffect(pagerState) {
                while (true) {
                    kotlinx.coroutines.delay(5000)
                    val nextPage = pagerState.currentPage + 1
                    pagerState.animateScrollToPage(nextPage)
                }
            }
            
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val actualIndex = page % leagues.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = leagues[actualIndex]),
                            contentDescription = "League Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Popular",
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Popular",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_popular_1788274756436),
                    contentDescription = "Popular Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Top Events",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Top Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(matches) { match ->
                    MatchCardView(
                        match = match,
                        modifier = Modifier.width(300.dp),
                        onMatchClick = onNavigateToMatch,
                        onBetClick = { team, odds ->
                            betTeam = team
                            betOdds = odds
                            betMatchTitle = match.title
                        }
                    )
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_worldcup_colorful_1788277140954),
                    contentDescription = "World Cup 2027",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun MatchCardView(match: UiMatch, modifier: Modifier = Modifier, onMatchClick: (String) -> Unit = {}, onBetClick: (String, Double) -> Unit = { _, _ -> }) {
    Card(
        modifier = modifier.clickable { onMatchClick(match.id) },
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = match.tournament, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Badge(
                    containerColor = if (match.isLive) LiveRed else UpcomingBlue
                ) {
                    Text(text = if (match.isLive) "LIVE" else "UPCOMING", modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Team 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = match.team1, fontWeight = FontWeight.Bold)
                if (match.score1.isNotEmpty()) {
                    Text(text = match.score1, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Team 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = match.team2, fontWeight = FontWeight.Bold)
                if (match.score2.isNotEmpty()) {
                    Text(text = match.score2, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = match.stateInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(12.dp))
            // Odds Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onBetClick(match.team1, match.odds1 ?: 0.0) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(text = "${match.team1} \n${match.odds1 ?: "-"}", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                Button(
                    onClick = { onBetClick(match.team2, match.odds2 ?: 0.0) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(text = "${match.team2} \n${match.odds2 ?: "-"}", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}
