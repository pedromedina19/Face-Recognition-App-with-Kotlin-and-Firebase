package com.example.facerecognitionandfirebaseapp.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.facerecognitionandfirebaseapp.data.model.ProcessedImage

@Composable
fun FaceAnalytics(image: ProcessedImage, modifier: Modifier = Modifier) = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.verticalScroll(rememberScrollState())
) {
    if (image.name.isNotEmpty()) Text(text = image.name)
    image.similarity?.let { InfoItem("Cosine Similarity", "${String.format("%.1f", it * 100F)}%") }
    image.distance?.let { InfoItem("Euclidean Distance", String.format("%.2f", it)) }
    image.spoof?.let { InfoItem("Real Probability", "${(it * 100).toInt()}%") }
    image.face?.let {
        InfoItem("Left Eye Open", "${((it.rightEyeOpenProbability ?: 0).toFloat() * 100).toInt()}%")
        InfoItem("Right Eye Open", "${((it.leftEyeOpenProbability ?: 0).toFloat() * 100).toInt()}%")
        InfoItem("Head Rotation X", "${String.format("%.2f", it.headEulerAngleX)}°")
        InfoItem("Had Rotation Y", "${String.format("%.2f", it.headEulerAngleY)}°")
        InfoItem("Head Rotation Z", "${String.format("%.2f", it.headEulerAngleZ)}°")
    }
}

@Composable
fun InfoItem(title: String, message: String) = Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(text = title, modifier = Modifier.weight(1f))
    Text(text = message, modifier = Modifier.weight(0.3f))
}