package com.example.facerecognitionandfirebaseapp.ui.screen.lock


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.facerecognitionandfirebaseapp.data.model.AppState
import com.example.facerecognitionandfirebaseapp.ui.navigation.Routes
import com.example.facerecognitionandfirebaseapp.ui.screen.recogniseFace.RecogniseFaceScreen


@Composable
fun LockScreen(appState: AppState, host: NavHostController,
               vm: LockViewModel = hiltViewModel(),
               builder: NavGraphBuilder.() -> Unit = lockNavGraphBuilder(appState, host),) {
    DisposableEffect(appState, host) {
        vm.onCompose(appState, host)
        onDispose { vm.onDispose() }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            modifier = Modifier
                .size(220.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary ,
                    shape = RoundedCornerShape(60.dp)
                ),
            onClick = {
                host.navigate(Routes.Recognise.path)
            Log.i("LOCKTESTE", "testando")}) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", Modifier.size(180.dp), MaterialTheme.colorScheme.primary) // Substitua por seu ícone
        }
    }
}

fun lockNavGraphBuilder(state: AppState, host: NavHostController): NavGraphBuilder.() -> Unit = {
    Routes.Recognise(this) { RecogniseFaceScreen(state, host) }
}