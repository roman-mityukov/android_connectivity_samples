package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BluetoothStatusServiceImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val permissionChecker: BluetoothPermissionChecker,
) : BluetoothStatusService {
    override val status: BluetoothStatus
        get() {
            val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager.adapter

            val bluetoothAvailable = applicationContext.packageManager.hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH
            )
            val regularRuntimePermissionsGranted =
                permissionChecker.regularRuntimePermissionsGranted
            val extraRuntimePermissionsGranted = permissionChecker.extraRuntimePermissionGranted

            val state = if (bluetoothAvailable.not()) {
                BluetoothStatus.NotSupported
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && applicationContext.packageManager.hasSystemFeature(
                    PackageManager.FEATURE_LOCATION
                ).not()
            ) {
                BluetoothStatus.LocationNotSupported
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && LocationManagerCompat.isLocationEnabled(
                    applicationContext.getSystemService(
                        Context.LOCATION_SERVICE
                    ) as LocationManager
                ).not()
            ) {
                BluetoothStatus.LocationDisabled
            } else if (regularRuntimePermissionsGranted.not()) {
                BluetoothStatus.RegularPermissionsNotGranted
            } else if (extraRuntimePermissionsGranted.not()) {
                BluetoothStatus.ExtraPermissionsNotGranted
            } else if (bluetoothAdapter.isEnabled.not()) {
                BluetoothStatus.Disabled
            } else {
                BluetoothStatus.Ok
            }
            return state
        }
}