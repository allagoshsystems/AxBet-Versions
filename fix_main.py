with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

import_replacement = """import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import com.example.ui.theme.MyApplicationTheme"""

content = content.replace("import androidx.core.content.ContextCompat\nimport com.example.ui.theme.MyApplicationTheme", import_replacement)

manager_instantiation = """  private lateinit var dataRepository: DataRepository
  private lateinit var authViewModel: AuthViewModel
  private lateinit var updateManager: AppUpdateManager"""

content = content.replace("  private lateinit var dataRepository: DataRepository\n  private lateinit var authViewModel: AuthViewModel", manager_instantiation)

manager_init = """    super.onCreate(savedInstanceState)
    dataRepository = DataRepository(this)
    authViewModel = AuthViewModel()
    updateManager = AppUpdateManager(this)"""

content = content.replace("    super.onCreate(savedInstanceState)\n    dataRepository = DataRepository(this)\n    authViewModel = AuthViewModel()", manager_init)

content_replacement = """    setContent {
      MyApplicationTheme {
        val updateInfo by updateManager.checkForUpdates().collectAsState(initial = null)
        var skipUpdate by remember { mutableStateOf(false) }
        
        if (updateInfo != null && !skipUpdate) {
            UpdateScreen(
                updateInfo = updateInfo!!,
                onUpdateClicked = {
                    updateManager.downloadAndInstallUpdate(updateInfo!!.downloadUrl) { progress ->
                        // Progress handled by system notification
                    }
                },
                onSkipClicked = { skipUpdate = true }
            )
        } else {
            AppNavigation(repository = dataRepository, authViewModel = authViewModel)
        }
      }
    }"""

content = content.replace("""    setContent {
      MyApplicationTheme {
        AppNavigation(repository = dataRepository, authViewModel = authViewModel)
      }
    }""", content_replacement)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
