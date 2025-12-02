package io.mityukov.connectivity.samples.core.connectivity.bclassic

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class DiscoveredDevice(val name: String?, val address: String)

sealed interface BluetoothDiscoveryProgress {
    data object Started : BluetoothDiscoveryProgress
    data object Finished : BluetoothDiscoveryProgress
}

data class BluetoothDiscoveryState(
    val progress: BluetoothDiscoveryProgress,
    val discoveredDevices: Set<DiscoveredDevice>,
)

sealed interface StartDiscoveryResult {
    data object Success : StartDiscoveryResult
    data class Failure(val status: BluetoothStatus) : StartDiscoveryResult
}

interface BluetoothDiscoveryService {
    val discoveryFlow: Flow<BluetoothDiscoveryState>
    suspend fun startDiscovery(): StartDiscoveryResult
    fun stopDiscovery()
    fun clear()
}