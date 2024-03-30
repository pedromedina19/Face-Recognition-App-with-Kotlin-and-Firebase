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
    

}