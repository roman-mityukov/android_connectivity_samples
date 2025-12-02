package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.connectivity.samples.core.common.DispatcherIO
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothConnectionService.Companion.sdpUuid
import io.mityukov.connectivity.samples.core.log.logd
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class BluetoothConnectionServiceImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher
) : BluetoothConnectionService {
    private val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter!!
    private var socket: BluetoothSocket? = null
    private var serverSocket: BluetoothServerSocket? = null

    override suspend fun connect(pairedDevice: PairedDevice) = withContext(dispatcher) {
        this@BluetoothConnectionServiceImpl.logd("socket connect")
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        if (socket != null) {
            socket?.close()
        }

        socket = bluetoothAdapter.getRemoteDevice(pairedDevice.address)
            .createRfcommSocketToServiceRecord(
                UUID.fromString(sdpUuid)
            )
        socket?.connect()
        this@BluetoothConnectionServiceImpl.logd("socket connected")
        socket?.outputStream?.write(4)
        Unit
    }

    private val mmBuffer: ByteArray = ByteArray(1024)

    override suspend fun listen() = withContext(dispatcher) {
        this@BluetoothConnectionServiceImpl.logd("serverSocket listen")
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
            "io.mityukov.connectivity.samples.SDP",
            UUID.fromString(sdpUuid)
        )
        val mySocket = serverSocket?.accept()
        this@BluetoothConnectionServiceImpl.logd("serverSocket accepted")
        launch {
            while (true) {
                val numBytes = mySocket?.inputStream?.read(mmBuffer)
                this@BluetoothConnectionServiceImpl.logd("numBytes $numBytes mmBuffer ${mmBuffer.firstOrNull()}")
            }
        }
        Unit
    }

    override fun cancel() {
        socket?.close()
        serverSocket?.close()
    }
}