package com.example.facerecognitionandfirebaseapp.data.model



data class FaceData(
    var id: Int?,
    var name: String,
    var timestamp: String
) {
    // Construtor sem argumentos necessário para o Firebase
    constructor() : this(null, "", "")
}