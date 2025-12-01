package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.Manifest
import android.os.Build

interface BluetoothPermissionChecker {
    companion object {
        val regularRuntimePermissions: List<String>
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                )
            } else {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            }
        val extraRuntimePermissions: List<String>
            get() = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ||
                Build.VERSION.SDK_INT == Build.VERSION_CODES.R
            ) {
                listOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
            } else {
                listOf()
            }
    }

    val regularRuntimePermissionsGranted: Boolean
    val extraRuntimePermissionGranted: Boolean
}