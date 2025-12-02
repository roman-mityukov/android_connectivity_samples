package io.mityukov.connectivity.samples.core.connectivity.bclassic

interface BluetoothConnectionService {
    companion object {
        val sdpUuid = "00001101-0000-1000-8000-00805F9B34FB"
    }
    suspend fun connect(pairedDevice: PairedDevice)
    suspend fun listen()
    fun cancel()
}