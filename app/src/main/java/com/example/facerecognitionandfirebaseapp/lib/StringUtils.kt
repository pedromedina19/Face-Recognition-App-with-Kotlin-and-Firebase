package com.example.facerecognitionandfirebaseapp.lib

object StringUtils {
    val String.spaced get():String = "(?<=.)[A-Z]".toRegex().replace(this) { " ${it.value}" }
    fun cleanPhoneNumber(string: String, telCode: String = "+55") = string.replace(" ", "").removePrefix(telCode).removePrefix("0")
}