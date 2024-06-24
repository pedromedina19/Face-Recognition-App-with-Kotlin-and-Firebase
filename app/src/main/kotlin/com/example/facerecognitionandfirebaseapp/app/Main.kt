package com.example.facerecognitionandfirebaseapp.app

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facerecognitionandfirebaseapp.lib.LOG
import com.example.facerecognitionandfirebaseapp.services.PushNotificationService
import com.example.facerecognitionandfirebaseapp.ui.navigation.AppHost
import com.example.facerecognitionandfirebaseapp.ui.navigation.Routes
import com.example.facerecognitionandfirebaseapp.ui.theme.AppTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

object Main {
    @HiltAndroidApp
    class HiltApp : Application() {
        override fun onCreate() {
            super.onCreate()

            // Create notification channel
            createNotificationChannel()
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Face Recognition Notifications"
                val descriptionText = "Notifications for face recognition events"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(PushNotificationService.CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    @AndroidEntryPoint
    class AppActivity : ComponentActivity() {

        private val REQUEST_POST_NOTIFICATIONS = 1

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setContent { AppContent() }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestNotificationPermission()
                }
            }

            Firebase.messaging.subscribeToTopic("all")
                .addOnCompleteListener { task ->
                    var msg = "Subscribed to all topic"
                    if (!task.isSuccessful) {
                        msg = "Subscription failed"
                    }
                    Log.d("FCM", msg)
                }

            // Obtain the FCM token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get the FCM token
                val token = task.result
                Log.d("FCM Token", token)
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun requestNotificationPermission() {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(this)
                    .setTitle("Notification Permission Needed")
                    .setMessage("This app needs the Notification permission to notify you about important updates and events. Please grant the permission to proceed.")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            REQUEST_POST_NOTIFICATIONS
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }
    }

    @Composable
    fun AppContent() = AppTheme(dynamicColors = true, statusBar = true) {
        runCatching { Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { AppHost(Routes.Home.path) } }
            .onFailure { LOG.e(it, it.message) }.exceptionOrNull()?.printStackTrace()
    }
}



