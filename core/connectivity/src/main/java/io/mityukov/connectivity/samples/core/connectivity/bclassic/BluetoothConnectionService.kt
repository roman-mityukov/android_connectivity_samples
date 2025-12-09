package io.mityukov.connectivity.samples.core.connectivity.bclassic

import kotlinx.coroutines.flow.Flow

sealed interface BluetoothConnection {
    data object None : BluetoothConnection
    data object Listen : BluetoothConnection
    data object Connecting : BluetoothConnection
    data object Connected : BluetoothConnection
}

interface BluetoothConnectionService {
    companion object {
        val sdpName = "io.mityukov.connectivity.samples.SDP"
        val sppUuid = "00001101-0000-1000-8000-00805F9B34FB"
    }

    val connectionState: Flow<BluetoothConnection>
    val message: Flow<String>
    fun connect(pairedDevice: PairedDevice)
    fun send(message: String)
    fun start()
    fun stop()
}