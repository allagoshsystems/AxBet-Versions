package com.example

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

fun isInternetAvailable(context: Context): Boolean {
    return try {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = connectivityManager.activeNetwork ?: return true
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return true
        activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
        true
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0.01f) }
    var percentage by remember { mutableIntStateOf(1) }
    var showNetworkError by remember { mutableStateOf(false) }
    var isRetrying by remember { mutableStateOf(false) }
    
    LaunchedEffect(showNetworkError, isRetrying) {
        if (showNetworkError) return@LaunchedEffect
        
        val totalDurationMs = 13000L
        val steps = 100
        val stepDuration = totalDurationMs / steps
        
        for (i in percentage..100) {
            if (i == 23) {
                if (!isInternetAvailable(context)) {
                    showNetworkError = true
                    return@LaunchedEffect
                }
            }
            percentage = i
            progress = i / 100f
            delay(stepDuration)
        }
        onSplashComplete()
    }
    
    if (showNetworkError) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = "No Internet",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Connection Lost",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Troubleshooting Guide:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Check your Wi-Fi or Mobile Data connection.", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Ensure Airplane Mode is turned off.", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Verify you have an active internet plan.", fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        if (isInternetAvailable(context)) {
                            showNetworkError = false
                            isRetrying = !isRetrying
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("RETRY CONNECTION", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Please connect to the internet to continue loading.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.SportsCricket,
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AXBET",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "CRICKET",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(64.dp))
                
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 6.dp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "$percentage%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (percentage < 23) {
                        "Initializing systems..."
                    } else if (percentage in 23..45) {
                        "Connecting to AXBET Cloud..."
                    } else if (percentage in 46..75) {
                        "Checking app version (v${BuildConfig.VERSION_NAME})..."
                    } else if (percentage in 76..92) {
                        "Loading match feeds & odds..."
                    } else {
                        "Preparing dashboard..."
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                )
            }
        }
    }
}
