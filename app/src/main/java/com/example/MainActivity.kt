package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private lateinit var dataRepository: DataRepository
  private lateinit var authViewModel: AuthViewModel
  private lateinit var updateManager: AppUpdateManager

  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
      // Permissions handled
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    dataRepository = DataRepository(this)
    authViewModel = AuthViewModel()
    updateManager = AppUpdateManager(this)
    
    val permissionsToRequest = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
    } else {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    
    if (permissionsToRequest.isNotEmpty()) {
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    enableEdgeToEdge()
    setContent {
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
    }
  }
}
