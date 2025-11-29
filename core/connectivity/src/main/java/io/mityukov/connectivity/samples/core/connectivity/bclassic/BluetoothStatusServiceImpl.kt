package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.bluetooth.BluetoothManager
import android.content.Context
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
            val permissionsAreGranted = permissionChecker.permissionsAreGranted
            val state = if (bluetoothAdapter == null) {
                BluetoothStatus.NotSupported
            } else if (permissionsAreGranted.not()) {
                BluetoothStatus.PermissionsNotGranted
            } else if (bluetoothAdapter.isEnabled.not()) {
                BluetoothStatus.Disabled
            } else {
                BluetoothStatus.Ok
            }
            return state
        }
}