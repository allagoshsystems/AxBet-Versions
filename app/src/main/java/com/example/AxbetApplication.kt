package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class AxbetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup global safety uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AXBET_CRASH", "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("AXBET_APP", "Firebase initialize error: ${e.message}", e)
        }
    }
}
