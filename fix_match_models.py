with open("app/src/main/java/com/example/Models.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val odds: Odds?",
    "val odds: Odds?,\n    val recent_balls: List<String>? = emptyList()"
)

content = content.replace(
    "val additionalMarkets: List<Market> = emptyList()",
    "val additionalMarkets: List<Market> = emptyList(),\n    val recentBalls: List<String> = emptyList()"
)

with open("app/src/main/java/com/example/Models.kt", "w") as f:
    f.write(content)
print("Updated Models.kt")
