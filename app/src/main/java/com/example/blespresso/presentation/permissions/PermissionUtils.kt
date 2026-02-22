package com.example.blespresso.presentation.permissions

import android.Manifest
import android.os.Build

// video 1-> Time-Stamp: 1:14:00
object PermissionUtils {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }else{
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}