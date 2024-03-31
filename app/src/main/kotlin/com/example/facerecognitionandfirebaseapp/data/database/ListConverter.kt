package com.example.facerecognitionandfirebaseapp.data.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.face.FaceLandmark

@ProvidedTypeConverter
class ListConverter {
    private val faceLandmarkListType = object : TypeToken<List<FaceLandmark>>() {}.type

    @TypeConverter
    fun toFaceLandmark(value: List<FaceLandmark>): String = Gson().toJson(value, faceLandmarkListType)

    @TypeConverter
    fun toFaceLandmarkList(value: String): List<FaceLandmark> = try {
        Gson().fromJson(value, faceLandmarkListType)
    } catch (e: Exception) {
        listOf()
    }

}