package io.mityukov.connectivity.samples.core.connectivity.bclassic

sealed interface BluetoothHealthState {
    data object NotSupported : BluetoothHealthState
    data object Disabled : BluetoothHealthState
    data object PermissionsNotGranted : BluetoothHealthState
    data object Ok : BluetoothHealthState
}

interface BluetoothHealthService {
    val bluetoothHealth: BluetoothHealthState
}