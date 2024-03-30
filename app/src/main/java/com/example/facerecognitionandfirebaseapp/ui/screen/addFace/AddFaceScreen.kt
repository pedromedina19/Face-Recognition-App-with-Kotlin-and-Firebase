package com.example.facerecognitionandfirebaseapp.ui.screen.addFace

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.facerecognitionandfirebaseapp.data.model.AppState
import com.example.facerecognitionandfirebaseapp.data.model.ProcessedImage
import com.example.facerecognitionandfirebaseapp.ui.composable.FaceAnalytics
import com.example.facerecognitionandfirebaseapp.ui.composable.FaceView
import com.example.facerecognitionandfirebaseapp.ui.composable.FrameView

@Composable
fun AddFaceScreen(appState: AppState, host: NavHostController, vm: AddFaceViewModel = hiltViewModel()) {
    val image: ProcessedImage by vm.image
    val lensFacing: Int by vm.lensFacing
    val showSaveDialog: Boolean by vm.showSaveDialog
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(showSaveDialog, lensFacing) {
        vm.onCompose(context, lifecycleOwner, appState.snackbar)
        onDispose { vm.onDispose() }
    }

    val content: @Composable (PaddingValues) -> Unit = { padding ->
        Column(
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            image.frame?.let { FrameView(frame = it, onFlipCamera = vm::onFlipCamera, modifier = Modifier.weight(1f)) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    image.face?.let {
                        FaceAnalytics(image, Modifier.weight(1f))
                        Button(vm::showSaveDialog) {
                            Text(text = "Capture Face")
                            Icon(Icons.Default.Add, "Capture Face Icon")
                        }
                    }
                }
            }
        }

    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = content
    )

    if (showSaveDialog) SaveDialog(image, onValueChange = vm::onNameChange, onCancel = vm::hideSaveDialog, onSave = vm::saveFace)
}


@Composable
private fun SaveDialog(
    value: ProcessedImage,
    modifier: Modifier = Modifier,
    title: String = "Save Captured Face",
    placeholder: String = "Face Name",
    positiveBtnText: String = "Save",
    negativeBtnText: String = "Cancel",
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    val newContent = content ?: {
        Card(modifier = modifier, elevation = CardDefaults.cardElevation()) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                value.faceBitmap?.asImageBitmap()?.let {
                    Image(it, contentDescription = null, alignment = Alignment.Center, contentScale = ContentScale.Fit, modifier = Modifier.size(250.dp))
                }
                OutlinedTextField(
                    value = value.name,
                    label = { Text(text = placeholder) },
                    placeholder = { Text(text = placeholder) },
                    onValueChange = onValueChange, modifier = Modifier
                        .fillMaxWidth()
                )
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                Text(
                    text = negativeBtnText.uppercase(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable(onClick = onCancel)
                        .weight(1f)
                )
                val isValid = value.name.length > 3
                Text(
                    text = positiveBtnText.uppercase(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable(enabled = isValid, onClick = onSave)
                        .weight(1f)
                )
            }
        }
    }
    Dialog(onCancel, properties, newContent)
}