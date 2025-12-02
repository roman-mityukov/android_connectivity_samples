package io.mityukov.connectivity.samples.core.connectivity.bclassic

import kotlinx.serialization.Serializable

@Serializable
data class PairedDevice(val name: String?, val address: String)

sealed interface PairedDevicesResult {
    data class Success(val devices: List<PairedDevice>) : PairedDevicesResult
    data class Failure(val status: BluetoothStatus) : PairedDevicesResult
}

sealed interface StartPairingResult {
    data object Success : StartPairingResult
    data object Failure : StartPairingResult
}

sealed interface EnsureDiscoverableResult {
    data object Success : EnsureDiscoverableResult
    data object AlreadyDiscoverable : EnsureDiscoverableResult
    data class Failure(val status: BluetoothStatus) : EnsureDiscoverableResult
}

interface BluetoothPairedDevicesService {
    suspend fun getPairedDevices(): PairedDevicesResult
    suspend fun ensureDiscoverable(): EnsureDiscoverableResult
    suspend fun startPairing(device: DiscoveredDevice): StartPairingResult
}