package com.example.facerecognitionandfirebaseapp.ui.screen.faces

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.facerecognitionandfirebaseapp.data.model.AppState
import com.example.facerecognitionandfirebaseapp.data.model.FaceInfo

@Composable
fun FacesScreen(appState: AppState, host: NavHostController, vm: FaceViewModel = hiltViewModel()) {
    val faces by vm.faces.collectAsState(mutableListOf())

    DisposableEffect(appState, host) {
        vm.onCompose(appState, host)
        onDispose { vm.onDispose() }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                Text(text = "Saved Faces")
                LazyColumn(
                    //verticalArrangement = Arrangement.Center,
                    //horizontalAlignment = Alignment.CenterHorizontally,
                ) { items(faces) { item -> FaceInfoItem(item, Modifier.padding(), vm::onDeleteFace) } }
            }
        }
    )
}


@Composable
private fun FaceInfoItem(face: FaceInfo, modifier: Modifier = Modifier, onDelete: (FaceInfo) -> Unit) = Card(modifier) {
    val ctx = LocalContext.current
    Row(Modifier.padding(), Arrangement.SpaceBetween) {
        face.faceBitmap(ctx)?.asImageBitmap()?.let { Image(it, "Face Bitmap", Modifier.size(50.dp)) }
        Column(Modifier.weight(1f)) {
            Text(text = face.name)
            Text(text = face.timestamp)
        }
        IconButton({ onDelete(face) }) { Icon(Icons.Default.Delete, null, Modifier.size(48.dp), MaterialTheme.colorScheme.error) }
    }
}