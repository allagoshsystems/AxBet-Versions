import re

with open("app/src/main/java/com/example/ProfileScreen.kt", "r") as f:
    content = f.read()

# Replace TabRow with ScrollableTabRow and add maxLines=1 to Tab text
content = content.replace("TabRow(selectedTabIndex = selectedTab) {", "ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {")
content = content.replace("text = { Text(title) }", "text = { Text(title, maxLines = 1) }")

with open("app/src/main/java/com/example/ProfileScreen.kt", "w") as f:
    f.write(content)
print("Updated ProfileScreen.kt")
