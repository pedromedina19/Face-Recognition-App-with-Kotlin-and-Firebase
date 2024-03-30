package com.example.facerecognitionandfirebaseapp.app

import android.app.Application
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

object Main {
    @HiltAndroidApp
    class HiltApp : Application()

    @AndroidEntryPoint
    class AppActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setContent { AppContent() }
        }

    }

    @Composable
    fun AppContent() {

    }
}