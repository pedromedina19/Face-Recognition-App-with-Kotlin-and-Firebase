package com.example.facerecognitionandfirebaseapp.ui.screen.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.facerecognitionandfirebaseapp.data.repositories.Repository
import com.example.facerecognitionandfirebaseapp.data.model.AppState
import com.example.facerecognitionandfirebaseapp.data.model.FaceInfo
import com.example.facerecognitionandfirebaseapp.lib.LOG
import com.example.facerecognitionandfirebaseapp.ui.navigation.Routes
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    val facesRef = Firebase.database.reference.child("faces")
    lateinit var homeHost: NavHostController
    lateinit var appState: AppState
    val faces: Flow<List<FaceInfo>> = repo.faces

    fun onCompose(state: AppState, home: NavHostController) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            appState = state
            homeHost = home
            LOG.d("Add Face Screen Composed")
        }.onFailure { LOG.e(it, it.message) }
    }

    fun onDispose() = runCatching {
        LOG.d("Add Face Screen Disposed")
    }.onFailure { LOG.e(it, it.message) }



    fun onDeleteFace(face: FaceInfo) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            repo.deleteFace(face).getOrNull()
            facesRef.child(face.id.toString()).removeValue()
            LOG.d("Deleted Face \t:\t$face")
        }.onFailure { LOG.e(it, it.message) }
    }
}
