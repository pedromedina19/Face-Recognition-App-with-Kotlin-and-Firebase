package com.example.facerecognitionandfirebaseapp.lib

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


object AiModel {
    private const val FACE_NET_MODEL_PATH = "face_net_512.tflite"
    private const val ANTI_SPOOF_MODEL_PATH = "anti_spoof_model.tflite"
    private const val MOBILE_NET_MODEL_PATH = "mobile_net.tflite"

    const val FACE_NET_IMAGE_SIZE = 160
    const val FACE_NET_EMBEDDING_SIZE = 512
    const val MOBILE_NET_IMAGE_SIZE = 224

    private const val IMAGE_MEAN = 128.0f
    private const val IMAGE_STD = 128.0f
    const val DEFAULT_SIMILARITY = 0.8f
    private var isRunning = false

    val Context.faceNetInterpreter
        get(): Interpreter {
            val fileDescriptor = assets.openFd(FACE_NET_MODEL_PATH)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            return Interpreter(modelBuffer)
        }

    val Context.mobileNetInterpreter
        get(): Interpreter {
            val fileDescriptor = assets.openFd(MOBILE_NET_MODEL_PATH)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            return Interpreter(modelBuffer)
        }

    val Context.antiSpoofInterpreter
        get(): Interpreter {
            val fileDescriptor = assets.openFd(ANTI_SPOOF_MODEL_PATH)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            return Interpreter(modelBuffer)
        }

    
}