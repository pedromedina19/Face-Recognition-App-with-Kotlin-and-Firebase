package com.example.facerecognitionandfirebaseapp.app

import android.app.Application
import androidx.room.Room
import com.example.facerecognitionandfirebaseapp.data.database.ListConverter
import com.example.facerecognitionandfirebaseapp.data.database.MainDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {
    @Provides
    @Singleton
    fun provideListConverter(): ListConverter = ListConverter()

    @Provides
    @Singleton
    fun provideDatabase(app: Application, listConverter: ListConverter): MainDatabase =
        Room.databaseBuilder(app, MainDatabase::class.java, "MainDatabase").addTypeConverter(listConverter).build()


}