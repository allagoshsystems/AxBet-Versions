with open("app/src/main/java/com/example/DataRepository.kt", "r") as f:
    content = f.read()
content = content.replace("additionalMarkets = if (isOddsHidden) emptyList() else match.odds?.additional_markets ?: emptyList()\n                )", "additionalMarkets = if (isOddsHidden) emptyList() else match.odds?.additional_markets ?: emptyList(),\n                    recentBalls = match.recent_balls ?: emptyList()\n                )")
with open("app/src/main/java/com/example/DataRepository.kt", "w") as f:
    f.write(content)
