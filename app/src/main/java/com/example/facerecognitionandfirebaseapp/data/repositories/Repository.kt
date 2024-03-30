package com.example.facerecognitionandfirebaseapp.data.repositories

import android.app.Application
import android.graphics.Paint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.facerecognitionandfirebaseapp.data.database.MainDatabase
import com.example.facerecognitionandfirebaseapp.data.model.FaceInfo
import com.example.facerecognitionandfirebaseapp.data.model.ProcessedImage
import com.example.facerecognitionandfirebaseapp.lib.AiModel.recognizeFace
import com.example.facerecognitionandfirebaseapp.lib.FileUtils.writeBitmap
import com.example.facerecognitionandfirebaseapp.lib.LOG
import com.example.facerecognitionandfirebaseapp.lib.MediaUtils.bitmap
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Repository(private val app: Application, private val db: MainDatabase) {
    // Flow of face information from the database
    val faces: Flow<List<FaceInfo>> = db.dao.faces()
    // Retrieve face information by ID
    fun faceInfo(id: Int): Flow<FaceInfo> = db.dao.face(id)
    // Retrieve a list of faces
    suspend fun faceList(): List<FaceInfo> = db.dao.faceList()
    // Save face data into the database
    suspend fun saveFace(image: ProcessedImage) = runCatching {
        // Retrieve face information from the image
        val info = image.faceInfo
        // Get existing face images from the database
        val images = db.dao.faceList().map { it.processedImage(app) }
        // Check for various conditions before saving the face
        if (image.faceBitmap == null) throw Throwable("Face is empty")
        if (images.find { image.name == it.name } != null) throw Throwable("Name Already Exist.")
        if ((app.recognizeFace(image, images)?.matchesCriteria == true)) throw Throwable("Face Already Exist.")
        // Write image files to disk
        image.faceBitmap.let { app.writeBitmap(info.faceFileName, it).getOrNull() }
        image.frame?.let { app.writeBitmap(info.frameFileName, it).getOrNull() }
        image.image?.let { app.writeBitmap(info.imageFileName, it).getOrNull() }
        // Insert face information into the database
        db.dao.insert(info)
    }.onFailure { LOG.e(it, it.message) }

    // Delete a face from the database
    suspend fun deleteFace(face: FaceInfo) = runCatching {
        if (face.id == null) throw Throwable("Invalid Face Id")
        // Delete face from database and corresponding image files
        db.dao.delete(face.id)
        app.deleteFile(face.faceFileName)
        app.deleteFile(face.frameFileName)
        app.deleteFile(face.imageFileName)
    }.onFailure { LOG.e(it, it.message) }

    // Clear all faces from the database
    suspend fun clearAllFaces() = runCatching {
        db.dao.clear()
    }.onFailure { LOG.e(it, it.message) }

    // Executor for camera operations
    val cameraExecutor: Executor by lazy { Executors.newSingleThreadExecutor() }
    // Future for obtaining the camera provider
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy { ProcessCameraProvider.getInstance(app) }
    // Face detector using ML Kit Vision
    val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    // Find the biggest face among detected faces
    fun biggestFace(faces: MutableList<Face>): Face? {
        var biggestFace: Face? = null
        var biggestFaceSize = 0
        for (face in faces) {
            val faceSize = face.boundingBox.height() * face.boundingBox.width()
            if (faceSize > biggestFaceSize) {
                biggestFaceSize = faceSize
                biggestFace = face
            }
        }
        return biggestFace
    }

    // Select camera based on lens facing
    fun cameraSelector(lensFacing: Int): CameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    // Configure image analysis for camera
    fun imageAnalysis(lensFacing: Int, paint: Paint, onData: (Result<ProcessedImage>) -> Unit): ImageAnalysis {
        val imageAnalyzer: ImageAnalysis.Analyzer = imageAnalyzer(lensFacing, paint, onData)
        val imageAnalysis = ImageAnalysis.Builder().apply {
            setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        }.build()
        imageAnalysis.setAnalyzer(cameraExecutor, imageAnalyzer)
        return imageAnalysis
    }
    // Analyzer for processing camera images
    fun imageAnalyzer(lensFacing: Int, paint: Paint, onFaceInfo: (Result<ProcessedImage>) -> Unit): ImageAnalysis.Analyzer =
        ImageAnalysis.Analyzer { imageProxy ->
            runCatching {
                @ExperimentalGetImage
                val mediaImage = imageProxy.image ?: throw Throwable("Unable to get Media Image")
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val bitmap = imageProxy.bitmap.getOrNull() ?: throw Throwable("Unable to get Bitmap")
                // Process image for face detection
                faceDetector.process(image)
                    .addOnSuccessListener(cameraExecutor) { onFaceInfo(processImage(lensFacing, it, bitmap, paint)) }
                    .addOnFailureListener(cameraExecutor) { LOG.e(it) }
                    .addOnCompleteListener { imageProxy.close() }
            }.onFailure { LOG.e(it, it.message) }
        }


}