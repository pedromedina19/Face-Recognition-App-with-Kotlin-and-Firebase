package com.example.facerecognitionandfirebaseapp.lib



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

    
}