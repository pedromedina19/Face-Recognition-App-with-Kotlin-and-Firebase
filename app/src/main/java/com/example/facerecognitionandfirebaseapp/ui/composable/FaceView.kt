package com.example.facerecognitionandfirebaseapp.ui.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun FaceView(bitmap: Bitmap, modifier: Modifier = Modifier) = Box(modifier.padding()) {
    Image(bitmap.asImageBitmap(), "Face Bitmap", Modifier.fillMaxSize())
}