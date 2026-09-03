with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.Modifier\nimport androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
