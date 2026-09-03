with open("app/src/main/java/com/example/MatchDetailScreen.kt", "r") as f:
    content = f.read()

# Replace the fake recent balls launched effect with the actual data from the JSON
target_balls_sim = """            if (match.isLive) {
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

real_balls_impl = """            if (match.isLive && match.recentBalls.isNotEmpty()) {
                RecentOversGrid(balls = match.recentBalls)
            }"""

content = content.replace(target_balls_sim, real_balls_impl)

# To enhance the scoreboard to show runs, wickets clearly, we will add a parser composable
scoreboard_injection = """@Composable
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
}"""

if "fun ScoreDisplay" not in content:
    content += "\n" + scoreboard_injection

# replace text(match.score1) with ScoreDisplay
old_score1 = """                            if (match.score1.isNotEmpty()) {
                                Text(match.score1, color = Color(0xFFF2C94C), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
                            }"""
new_score1 = """                            if (match.score1.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                ScoreDisplay(score = match.score1)
                            }"""
content = content.replace(old_score1, new_score1)

old_score2 = """                            if (match.score2.isNotEmpty()) {
                                Text(match.score2, color = Color(0xFFF2C94C), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
                            }"""
new_score2 = """                            if (match.score2.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                ScoreDisplay(score = match.score2)
                            }"""
content = content.replace(old_score2, new_score2)


with open("app/src/main/java/com/example/MatchDetailScreen.kt", "w") as f:
    f.write(content)
print("Updated MatchDetailScreen.kt")
