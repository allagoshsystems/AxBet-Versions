with open("app/src/main/java/com/example/DataRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val additionalMarkets = if (isOddsHidden) emptyList() else match.odds?.additional_markets ?: emptyList()",
    "val additionalMarkets = if (isOddsHidden) emptyList() else match.odds?.additional_markets ?: emptyList(),\n                    recentBalls = match.recent_balls ?: emptyList()"
)

with open("app/src/main/java/com/example/DataRepository.kt", "w") as f:
    f.write(content)
print("Updated DataRepository.kt")
