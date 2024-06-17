package com.example.facerecognitionandfirebaseapp.ui.screen.home

import android.app.Activity
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.Keep
import androidx.navigation.NavHostController
import com.example.facerecognitionandfirebaseapp.R
import com.example.facerecognitionandfirebaseapp.ui.navigation.Routes
import com.example.facerecognitionandfirebaseapp.ui.screen.permission.Permission
import com.example.facerecognitionandfirebaseapp.ui.screen.permission.PermissionProvider

@Keep
data class HomeScreenState(
    val host: NavHostController? = null,
    val backPressDispatcher: OnBackPressedDispatcher? = null,
    val bottomBarItem: NavBarItem = defaultBottomBarItem,
    val bottomBarItems: List<NavBarItem> = defaultBottomBarItems,
    val permissions: MutableMap<PermissionProvider, Boolean> = defaultPermissions,
) {
    fun firstPermission(activity: Activity): PermissionProvider? = permissions.keys.firstOrNull { !it.hasPermission(activity) }

    companion object {
        val defaultPermissions: MutableMap<PermissionProvider, Boolean> = mutableMapOf(Permission.Camera to false)
        val defaultBottomBarItem: NavBarItem = NavBarItem(Routes.Lock, R.drawable.ic_home)
        val defaultBottomBarItems: List<NavBarItem> = listOf(
            defaultBottomBarItem,
            NavBarItem(Routes.AddFace, R.drawable.ic_add_face),
            NavBarItem(Routes.Faces, R.drawable.ic_faces),
            NavBarItem(Routes.Logs, R.drawable.ic_settings)
//        NavBarItem(Routes.Settings, R.drawable.ic_settings),
        )
    }
}
