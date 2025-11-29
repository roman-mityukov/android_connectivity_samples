package io.mityukov.connectivity.samples.core.connectivity.bclassic

sealed interface BluetoothStatus {
    data object NotSupported : BluetoothStatus
    data object Disabled : BluetoothStatus
    data object PermissionsNotGranted : BluetoothStatus
    data object Ok : BluetoothStatus
}

interface BluetoothStatusService {
    val status: BluetoothStatus
}