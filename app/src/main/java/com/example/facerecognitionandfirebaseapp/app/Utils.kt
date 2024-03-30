package com.example.facerecognitionandfirebaseapp.app

import android.icu.text.SimpleDateFormat
import android.os.Build
import java.util.*

object Utils {
    fun sdk(version: Int): Boolean = Build.VERSION.SDK_INT >= version
    fun timestamp(pattern: String = "yyyy-MM-dd HH:mm:ss", date: Date = Date()): String = SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}