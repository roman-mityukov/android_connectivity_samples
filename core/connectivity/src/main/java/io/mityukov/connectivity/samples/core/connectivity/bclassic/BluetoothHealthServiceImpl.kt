package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BluetoothHealthServiceImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val permissionChecker: BluetoothPermissionChecker,
) : BluetoothHealthService {
    override val bluetoothHealth: BluetoothHealthState
        get() {
            val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager.adapter
            val permissionsAreGranted = permissionChecker.permissionsAreGranted
            val state = if (bluetoothAdapter == null) {
                BluetoothHealthState.NotSupported
            } else if (permissionsAreGranted.not()) {
                BluetoothHealthState.PermissionsNotGranted
            } else if (bluetoothAdapter.isEnabled.not()) {
                BluetoothHealthState.Disabled
            } else {
                BluetoothHealthState.Ok
            }
            return state
        }
}