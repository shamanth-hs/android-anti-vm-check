package com.shamanth.antivm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat


object PermissionUtils {
    var PERMISSION = arrayOf<String>(Manifest.permission.READ_PHONE_STATE)
    fun isLacksOfPermission(context: Context, permission: String?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context.getApplicationContext(), permission!!
            ) == PackageManager.PERMISSION_DENIED
        } else false
    }
}