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
    if (image.name.isNotEmpty()) Text(text = image.name, style = MaterialTheme.typography.titleMedium)
    image.similarity?.let { InfoItem("Similaridade", "${String.format("%.1f", it * 100F)}%") }
    image.distance?.let { InfoItem("Distância euclidiana", String.format("%.2f", it)) }
    //image.spoof?.let { InfoItem("Probabilidade real", "${(it * 100).toInt()}%") }
    image.face?.let {
        InfoItem("Olho esquerdo aberto", "${((it.rightEyeOpenProbability ?: 0).toFloat() * 100).toInt()}%")
        InfoItem("Olho direito aberto", "${((it.leftEyeOpenProbability ?: 0).toFloat() * 100).toInt()}%")
        InfoItem("Rotação do rosto X", "${String.format("%.2f", it.headEulerAngleX)}°")
        InfoItem("Rotação do rosto Y", "${String.format("%.2f", it.headEulerAngleY)}°")
        InfoItem("Rotação do rosto Z", "${String.format("%.2f", it.headEulerAngleZ)}°")
    }
}

@Composable
fun InfoItem(title: String, message: String) = Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    Text(text = message, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(0.3f))
}