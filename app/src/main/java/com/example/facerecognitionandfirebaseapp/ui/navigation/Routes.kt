package com.example.facerecognitionandfirebaseapp.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class Routes : Route {
    Home, AddFace, Recognise, Faces {
        override val pattern: String get() = "${name}Screen?id={id}"
        override val arguments: List<NamedNavArgument> get() = listOf(navArgument("id") { defaultValue = "";type = NavType.StringType; })
    }
}