package com.example.facerecognitionandfirebaseapp.data.repositories

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
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
import com.example.facerecognitionandfirebaseapp.lib.MediaUtils.crop
import com.example.facerecognitionandfirebaseapp.lib.MediaUtils.flip
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.atan2

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

    // Process image for face detection and alignment
    fun processImage(
        lensFacing: Int,
        data: MutableList<Face>,
        bitmap: Bitmap,
        paint: Paint
    ): Result<ProcessedImage> = runCatching {
        // Set up paint for drawing on the image
        paint.style = Paint.Style.STROKE
        val face = biggestFace(data)
        var frame = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        var faceBitmap = face?.boundingBox?.let { bitmap.crop(it.left, it.top, it.width(), it.height()).getOrNull() }
        val canvas = Canvas(frame)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        data.forEach { canvas.drawRect(it.boundingBox, paint) }
        face?.allLandmarks?.forEach { canvas.drawPoint(it.position.x, it.position.y, paint) }
        // Flip the image horizontally if it's a front-facing camera
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            frame = frame.flip(horizontal = true).getOrNull()
            faceBitmap = faceBitmap?.flip(horizontal = true)?.getOrNull()
        }
        // Align face bitmap by facial landmarks
        faceBitmap = faceBitmap?.let { alignBitmapByLandmarks(bitmap = it, face?.allLandmarks ?: listOf()).getOrNull() }
        // Return processed image data
        return@runCatching ProcessedImage(image = bitmap, frame = frame, face = face, trackingId = face?.trackingId, faceBitmap = faceBitmap)
    }.onFailure { LOG.e(it, it.message) }

    // Function to align a bitmap based on facial landmarks
    fun alignBitmapByLandmarks(bitmap: Bitmap, landmarks: List<FaceLandmark>, noseRatio: Float = 0.4f, eyeDistanceRatio: Float = 0.3f): Result<Bitmap> = runCatching {
        val leftEye = landmarks.find { it.landmarkType == FaceLandmark.LEFT_EYE }?.position
        val rightEye = landmarks.find { it.landmarkType == FaceLandmark.RIGHT_EYE }?.position
        val noseBase = landmarks.find { it.landmarkType == FaceLandmark.NOSE_BASE }?.position

        if (leftEye == null || rightEye == null || noseBase == null) return@runCatching bitmap

        val matrix = Matrix()

        val eyeCenterX = (leftEye.x + rightEye.x) / 2f
        val eyeCenterY = (leftEye.y + rightEye.y) / 2f
        val dx = rightEye.x - leftEye.x
        val dy = rightEye.y - leftEye.y
        val angle = atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI

        matrix.postTranslate(-eyeCenterX, -eyeCenterY)
        matrix.postRotate(angle.toFloat(), 0f, 0f)

        // Calculate the desired eye distance based on a fixed ratio
        val desiredEyeDistance = bitmap.width * eyeDistanceRatio

        val scale = desiredEyeDistance / dx
        matrix.postScale(scale, scale)

        // Calculate the translation to bring the nose base to a fixed position
        val targetNoseY = bitmap.height * noseRatio
        val translationY = targetNoseY - noseBase.y * scale
        matrix.postTranslate(0f, translationY)

        // Apply the transformation matrix to the bitmap
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }.onFailure { LOG.e(it, it.message) }

}