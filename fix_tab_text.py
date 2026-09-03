import re

with open("app/src/main/java/com/example/ProfileScreen.kt", "r") as f:
    content = f.read()

# Make sure softWrap = false is added to the Tab text to prevent ANY kind of wrapping.
content = content.replace(
    "text = { Text(title, maxLines = 1) }",
    "text = { Text(text = title, maxLines = 1, softWrap = false) }"
)

with open("app/src/main/java/com/example/ProfileScreen.kt", "w") as f:
    f.write(content)
print("Updated ProfileScreen.kt tabs")
