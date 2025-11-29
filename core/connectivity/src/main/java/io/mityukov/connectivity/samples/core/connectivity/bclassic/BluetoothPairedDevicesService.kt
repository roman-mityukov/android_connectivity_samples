package io.mityukov.connectivity.samples.core.connectivity.bclassic

data class PairedDevice(val name: String?, val address: String)

sealed interface PairedDevicesResult {
    data class Success(val devices: List<PairedDevice>) : PairedDevicesResult
    data class Failure(val status: BluetoothStatus) : PairedDevicesResult
}

sealed interface EnsureDiscoverableResult {
    data object Success : EnsureDiscoverableResult
    data object AlreadyDiscoverable : EnsureDiscoverableResult
    data class Failure(val status: BluetoothStatus) : EnsureDiscoverableResult
}

interface BluetoothPairedDevicesService {
    suspend fun getPairedDevices(): PairedDevicesResult
    suspend fun ensureDiscoverable(): EnsureDiscoverableResult
}