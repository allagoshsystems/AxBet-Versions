import re

with open("app/src/main/java/com/example/MatchDetailScreen.kt", "r") as f:
    content = f.read()

# Add new imports
new_imports = [
    "import androidx.compose.foundation.lazy.LazyRow",
    "import androidx.compose.foundation.lazy.items",
    "import androidx.compose.foundation.shape.CircleShape",
    "import androidx.compose.material.icons.filled.ArrowUpward",
    "import androidx.compose.material.icons.filled.ArrowDownward",
    "import androidx.compose.runtime.mutableDoubleStateOf",
    "import androidx.compose.runtime.mutableIntStateOf",
    "import androidx.compose.foundation.lazy.grid.LazyVerticalGrid",
    "import androidx.compose.foundation.lazy.grid.GridCells",
    "import androidx.compose.foundation.lazy.grid.items"
]

for imp in new_imports:
    if imp not in content:
        content = content.replace("import androidx.compose.foundation.layout.*", f"import androidx.compose.foundation.layout.*\n{imp}")

# Check if double state is already imported as part of previous changes (just in case)
if "import androidx.compose.runtime.mutableDoubleStateOf" not in content and "import androidx.compose.runtime.*" not in content:
    content = content.replace("import androidx.compose.runtime.mutableStateOf", "import androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.mutableDoubleStateOf\nimport androidx.compose.runtime.mutableIntStateOf")

new_components = """
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
"""

if "fun RecentOversGrid" not in content:
    content += new_components

# Update OddsButton
old_odds_button = """@Composable
fun OddsButton(
    teamName: String,
    odds: Double,
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
            Text(
                text = odds.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}"""

new_odds_button = """@Composable
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
}"""

content = content.replace(old_odds_button, new_odds_button)

# Replace OddsButton with TrendingOddsButton
content = content.replace("OddsButton(\n                            teamName = match.team1", "TrendingOddsButton(\n                            teamName = match.team1")
content = content.replace("OddsButton(\n                            teamName = match.team2", "TrendingOddsButton(\n                            teamName = match.team2")
content = content.replace("OddsButton(\n                                        teamName = market.selection", "TrendingOddsButton(\n                                        teamName = market.selection")


header_end_target = """                    Text(
                        text = match.stateInfo,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }"""

recent_balls_injection = """                    Text(
                        text = match.stateInfo,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            if (match.isLive) {
                var recentBalls by remember { mutableStateOf(listOf("0", "1", "W", "4", "wd", "1", "6", "2", "0", "1lb", "4", "1")) }
                LaunchedEffect(Unit) {
                    val possibleBalls = listOf("0", "1", "2", "4", "6", "W", "wd", "nb", "lb")
                    while(true) {
                        kotlinx.coroutines.delay(10000L) // Simulate a new ball every 10 seconds
                        val nextBall = possibleBalls.random()
                        recentBalls = (recentBalls + nextBall).takeLast(12)
                    }
                }
                RecentOversGrid(balls = recentBalls)
            }"""

content = content.replace(header_end_target, recent_balls_injection)

with open("app/src/main/java/com/example/MatchDetailScreen.kt", "w") as f:
    f.write(content)
print("Updated MatchDetailScreen.kt")
