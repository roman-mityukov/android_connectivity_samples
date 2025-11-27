package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BluetoothPermissionCheckerImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context
) : BluetoothPermissionChecker {
    override val permissionsAreGranted: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        }
}