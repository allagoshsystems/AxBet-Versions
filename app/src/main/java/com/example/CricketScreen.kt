package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CricketScreen(repository: DataRepository, authViewModel: AuthViewModel, onNavigateToMatch: (String) -> Unit) {
    val matches by repository.getLiveMatches().collectAsState(initial = emptyList())
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Live", "Upcoming", "Result")
    
    var betTeam by remember { mutableStateOf<String?>(null) }
    var betOdds by remember { mutableStateOf(0.0) }
    var betMatchTitle by remember { mutableStateOf("") }
    
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
    
    val filteredMatches = matches.filter { match ->
        when (selectedTabIndex) {
            0 -> match.stage == "live"
            1 -> match.stage == "upcoming"
            2 -> match.stage == "finished" || match.stage == "result"
            else -> true
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Cricket Matches",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (filteredMatches.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No matches found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredMatches) { match ->
                    MatchCardView(
                        match = match,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
    }
}
