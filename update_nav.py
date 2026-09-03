import re

with open("app/src/main/java/com/example/Navigation.kt", "r") as f:
    content = f.read()

# Add imports if missing
imports = [
    "import androidx.compose.runtime.mutableStateOf",
    "import androidx.compose.runtime.remember",
    "import androidx.compose.runtime.setValue"
]

for imp in imports:
    if imp not in content:
        content = content.replace("import androidx.compose.runtime.collectAsState", f"import androidx.compose.runtime.collectAsState\n{imp}")

# Update AppNavigation
target = """fun AppNavigation(repository: DataRepository, authViewModel: AuthViewModel) {
    val user by authViewModel.user.collectAsState()"""

replacement = """fun AppNavigation(repository: DataRepository, authViewModel: AuthViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
        return
    }

    val user by authViewModel.user.collectAsState()"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/Navigation.kt", "w") as f:
    f.write(content)
print("Updated Navigation.kt")
