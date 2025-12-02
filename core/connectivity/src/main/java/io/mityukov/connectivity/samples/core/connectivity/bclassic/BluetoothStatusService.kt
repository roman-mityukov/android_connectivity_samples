package io.mityukov.connectivity.samples.core.connectivity.bclassic

sealed interface BluetoothStatus {
    data object NotSupported : BluetoothStatus
    data object LocationNotSupported : BluetoothStatus
    data object Disabled : BluetoothStatus
    data object LocationDisabled : BluetoothStatus
    data object RegularPermissionsNotGranted : BluetoothStatus
    data object ExtraPermissionsNotGranted : BluetoothStatus
    data object Ok : BluetoothStatus
}

interface BluetoothStatusService {
    val status: BluetoothStatus
}