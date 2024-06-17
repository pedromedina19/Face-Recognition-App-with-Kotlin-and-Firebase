package com.example.facerecognitionandfirebaseapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.facerecognitionandfirebaseapp.data.model.AppState
import com.example.facerecognitionandfirebaseapp.ui.screen.addFace.AddFaceScreen
import com.example.facerecognitionandfirebaseapp.ui.screen.faces.FacesScreen
import com.example.facerecognitionandfirebaseapp.ui.screen.lock.LockScreen
import com.example.facerecognitionandfirebaseapp.ui.screen.logs.LogsScreen
import com.example.facerecognitionandfirebaseapp.ui.screen.recogniseFace.RecogniseFaceScreen

@Composable
fun HomeHost(
    state: AppState,
    startDestination: String,
    modifier: Modifier = Modifier,
    route: String? = null,
    host: NavHostController = rememberNavController(),
    builder: NavGraphBuilder.() -> Unit = homeNavGraphBuilder(state, host),
) = NavHost(host, startDestination, modifier, route, builder)

fun homeNavGraphBuilder(state: AppState, host: NavHostController): NavGraphBuilder.() -> Unit = {
    Routes.Faces(this) { FacesScreen(state, host) }
    Routes.Recognise(this) { RecogniseFaceScreen(state, host)}
    Routes.AddFace(this) { AddFaceScreen(state, host) }
    Routes.Lock(this) { LockScreen(state, host) }
    Routes.Logs(this){ LogsScreen(state , host)}
}

