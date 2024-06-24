package com.example.facerecognitionandfirebaseapp.ui.screen.recogniseFace

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facerecognitionandfirebaseapp.data.model.FaceData
import com.example.facerecognitionandfirebaseapp.data.repositories.Repository
import com.example.facerecognitionandfirebaseapp.data.model.ProcessedImage
import com.example.facerecognitionandfirebaseapp.lib.AiModel.mobileNet
import com.example.facerecognitionandfirebaseapp.lib.AiModel.recognizeFace
import com.example.facerecognitionandfirebaseapp.lib.LOG
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class RecogniseFaceViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    val lockref = Firebase.database.getReference("shouldOpenLock")
    lateinit var imageAnalysis: ImageAnalysis
    lateinit var lifecycleOwner: LifecycleOwner
    private val _doorIsOpen = MutableStateFlow(false)
    val doorIsOpen: StateFlow<Boolean> get() = _doorIsOpen
    private val doorIsOpenRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("doorIsOpen")
    private var wasDoorOpen: Boolean = false
    val cameraProvider: ProcessCameraProvider by lazy { repo.cameraProviderFuture.get() }
    var images: List<ProcessedImage> = listOf()
    val image: MutableState<ProcessedImage> = mutableStateOf(ProcessedImage())
    val showDialog: MutableState<Boolean> = mutableStateOf(false)
    var recognizedFace: MutableState<ProcessedImage?> = mutableStateOf(null)
    val lensFacing: MutableState<Int> = mutableStateOf(CameraSelector.LENS_FACING_FRONT)
    val cameraSelector get(): CameraSelector = repo.cameraSelector(lensFacing.value)
    val paint = Paint().apply {
        strokeWidth = 3f
        color = Color.BLUE
    }

    init {
        // Adicionar listener para mudanças em doorIsOpen
        doorIsOpenRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOpen = snapshot.getValue(Boolean::class.java) ?: false
                if (!isOpen && wasDoorOpen) {
                    val timestamp = System.currentTimeMillis()
                    val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date(timestamp)
                    )
                    saveLogToFirebase(FaceData(0, "Tranca Fechada", formattedTimestamp))
                }
                wasDoorOpen = isOpen
                _doorIsOpen.value = isOpen
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao ler estado da porta: ${error.message}")
            }
        })
    }

    val Context.getImageAnalysis
        get() = repo.imageAnalysis(lensFacing.value, paint) { result ->
            runCatching {
                val data = result.getOrNull() ?: return@runCatching
                data.landmarks = data.face?.allLandmarks ?: listOf()
                image.value = data
                recognizedFace.value = recognizeFace(data, images)
                recognizedFace.value = recognizedFace.value?.copy(spoof = mobileNet(data).getOrNull())

            }.onFailure { LOG.e(it, it.message) }
        }

    fun onCompose(context: Context, owner: LifecycleOwner) = viewModelScope.launch {
        runCatching {
            lifecycleOwner = owner
            imageAnalysis = context.getImageAnalysis
            images = withContext(Dispatchers.IO) { repo.faceList().map { it.processedImage(context) } }
            bindCamera()
            delay(1000)
            bindCamera()
            LOG.d("Recognise Face Screen Composed")
        }.onFailure { LOG.e(it, it.message) }
    }

    fun onDispose() = runCatching {
        cameraProvider.unbindAll()
        LOG.d("Recognise Face Screen Disposed")
    }.onFailure { LOG.e(it, it.message) }

    fun onFlipCamera() = runCatching {
        lensFacing.value = if (lensFacing.value == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        LOG.d("Camera Flipped lensFacing\t:\t${lensFacing.value}")
    }.onFailure { LOG.e(it, it.message) }

    fun bindCamera() = runCatching {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
        LOG.d("Camera is bound to lifecycle.")
    }.onFailure { LOG.e(it, it.message) }

    fun showDialog() = runCatching {
        wasDoorOpen = true
        showDialog.value = true
        lockref.setValue(true)
        cameraProvider.unbindAll()
    }.onFailure { LOG.e(it, it.message) }

    fun hideDialog(recognised: ProcessedImage, frame: ProcessedImage) = runCatching {
        saveLogToFirebase(FaceData(
            recognised.copy(face = frame.face).id,
            recognised.copy(face = frame.face).name,
            recognised.copy(face = frame.face).timestamp
        ))
        showDialog.value = false
        lockref.setValue(false)
        bindCamera()
    }.onFailure { LOG.e(it, it.message) }

    private fun saveLogToFirebase(log: FaceData) {
        val logsRef = Firebase.database.reference.child("logs")
        logsRef.push().setValue(log)
    }
}

