package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow

sealed interface DiscoveryProgress {
    data object Started : DiscoveryProgress
    data object Finished : DiscoveryProgress
}

data class DiscoveryState(
    val progress: DiscoveryProgress,
    val pairedDevices: Set<BluetoothDevice>,
    val discoveredDevices: Set<BluetoothDevice>,
)

interface BluetoothClassicChatService {
    val discoveryFlow: Flow<DiscoveryState>
    fun ensureDiscoverable()
    fun discover()
}