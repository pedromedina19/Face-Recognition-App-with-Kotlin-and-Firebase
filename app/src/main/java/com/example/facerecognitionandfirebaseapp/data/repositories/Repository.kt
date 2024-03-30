package com.example.facerecognitionandfirebaseapp.data.repositories

import android.app.Application
import com.example.facerecognitionandfirebaseapp.data.database.MainDatabase
import com.example.facerecognitionandfirebaseapp.data.model.FaceInfo
import com.example.facerecognitionandfirebaseapp.data.model.ProcessedImage
import com.example.facerecognitionandfirebaseapp.lib.AiModel.recognizeFace
import com.example.facerecognitionandfirebaseapp.lib.FileUtils.writeBitmap
import com.example.facerecognitionandfirebaseapp.lib.LOG
import kotlinx.coroutines.flow.Flow

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

    

}