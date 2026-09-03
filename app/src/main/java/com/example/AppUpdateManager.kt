package com.example

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

data class AppUpdateInfo(
    val latestVersionCode: Int = 0,
    val whatsNew: String = "",
    val downloadUrl: String = "",
    val isMandatory: Boolean = false,
    val currentVersionCode: Int = 1
)

class AppUpdateManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun checkForUpdates(): Flow<AppUpdateInfo?> = callbackFlow {
        // Read current version code
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            packageInfo.versionCode
        }

        val listener = db.collection("app_config").document("update_info")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val latestVersionCodeObj = snapshot.get("latest_version_code")
                        val latestVersionCode = when (latestVersionCodeObj) {
                            is Number -> latestVersionCodeObj.toInt()
                            is String -> latestVersionCodeObj.toIntOrNull() ?: 0
                            else -> 0
                        }
                        val whatsNew = snapshot.getString("whats_new") ?: ""
                        val downloadUrl = snapshot.getString("apk_download_url") ?: ""
                        val isMandatory = snapshot.getBoolean("is_mandatory") ?: false

                        if (latestVersionCode > currentVersionCode && downloadUrl.isNotEmpty()) {
                            trySend(
                                AppUpdateInfo(
                                    latestVersionCode = latestVersionCode,
                                    whatsNew = whatsNew,
                                    downloadUrl = downloadUrl,
                                    isMandatory = isMandatory,
                                    currentVersionCode = currentVersionCode
                                )
                            )
                        } else {
                            trySend(null)
                        }
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    fun downloadAndInstallUpdate(url: String, onProgress: (Float) -> Unit) {
        val fileName = "update_${System.currentTimeMillis()}.apk"
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading Update")
            .setDescription("Please wait while the new version is downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {
                    installApk(fileName)
                    context.unregisterReceiver(this)
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        // We can poll the progress using a thread or coroutine if needed.
        // For simplicity, we just trigger install when broadcast receiver fires.
    }

    private fun installApk(fileName: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.applicationContext.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
