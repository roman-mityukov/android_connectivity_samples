package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BluetoothPermissionCheckerImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context
) : BluetoothPermissionChecker {

    override val regularRuntimePermissionsGranted: Boolean
        get() = BluetoothPermissionChecker.regularRuntimePermissions.all { permission ->
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

    override val extraRuntimePermissionGranted: Boolean
        get() = if (BluetoothPermissionChecker.extraRuntimePermissions.isEmpty()) {
            true
        } else {
            BluetoothPermissionChecker.extraRuntimePermissions.all { permission ->
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
}