@file:SuppressLint("MissingPermission")

package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.connectivity.samples.core.common.DispatcherIO
import io.mityukov.connectivity.samples.core.log.logd
import io.mityukov.connectivity.samples.core.log.logw
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject


class BluetoothConnectionServiceImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher
) : BluetoothConnectionService {
    private val bluetoothAdapter =
        applicationContext.getSystemService(BluetoothManager::class.java).adapter!! // bluetooth is required in AndroidManifest.xml
    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    @Synchronized
    override fun start() {
        if (connectThread != null) {
            connectThread?.cancel()
            connectThread = null
        }

        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }

        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread?.start()
        }
    }

    override fun stop() {
        if (connectThread != null) {
            connectThread?.cancel()
            connectThread = null
        }

        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }

        if (acceptThread != null) {
            acceptThread?.cancel()
            acceptThread = null
        }
        mutableConnectionState.update {
            BluetoothConnection.None
        }
    }

    override fun connect(pairedDevice: PairedDevice) {
        if (mutableConnectionState.value == BluetoothConnection.Connecting) {
            connectThread?.cancel()
            connectThread = null
        }

        connectedThread?.cancel()
        connectedThread = null

        connectThread = ConnectThread(bluetoothAdapter.getRemoteDevice(pairedDevice.address))
        connectThread?.start()
    }

    private fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        if (connectThread != null) {
            connectThread?.cancel()
            connectThread = null
        }

        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }

        if (acceptThread != null) {
            acceptThread?.cancel()
            acceptThread = null
        }

        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
    }

    private fun connectionFailed() {
        mutableConnectionState.update {
            BluetoothConnection.None
        }
        start()
    }

    private fun connectionLost() {
        mutableConnectionState.update {
            BluetoothConnection.None
        }
        start()
    }

    private val mutableConnectionState =
        MutableStateFlow<BluetoothConnection>(BluetoothConnection.None)
    override val connectionState: Flow<BluetoothConnection> = mutableConnectionState.asStateFlow()

    private val mutableMessageFlow = MutableStateFlow("Test")
    override val message: Flow<String> = mutableMessageFlow.asStateFlow()

    override fun send(message: String) {
        connectedThread?.write(message.toByteArray())
    }

    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket

        init {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                BluetoothConnectionService.sdpName,
                UUID.fromString(BluetoothConnectionService.sppUuid),
            )
            mutableConnectionState.update {
                BluetoothConnection.Listen
            }
        }

        override fun run() {
            var socket: BluetoothSocket?

            // Listen to the server socket if we're not connected
            while (mutableConnectionState.value != BluetoothConnection.Connected) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = serverSocket.accept()
                    logd("accepted")
                } catch (e: IOException) {
                    logw("accept() failed $e ${e.stackTraceToString()}")
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothConnectionServiceImpl) {
                        when (mutableConnectionState.value) {
                            BluetoothConnection.Listen, BluetoothConnection.Connecting -> {
                                logd("connected")
                                connected(socket, socket.remoteDevice)
                            }// Situation normal. Start the connected thread.


                            BluetoothConnection.None, BluetoothConnection.Connected -> // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    logw("Could not close unwanted socket")
                                }
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                serverSocket.close()
            } catch (e: IOException) {
                logw("close of server failed $e ${e.stackTraceToString()}")
            }
        }
    }

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString(BluetoothConnectionService.sppUuid)
                )
            } catch (e: IOException) {
                logw("create failed ${e.stackTraceToString()}")
            }
            mutableConnectionState.update {
                BluetoothConnection.Connecting
            }
        }

        override fun run() {
            bluetoothAdapter.cancelDiscovery()

            try {
                socket?.connect()
            } catch (io1: IOException) {
                logw("connect() failed ${io1.stackTraceToString()}")
                try {
                    socket?.close()
                } catch (io2: IOException) {
                    logw("close() failed ${io2.stackTraceToString()}")
                }
                connectionFailed()
                return
            }

            synchronized(this@BluetoothConnectionServiceImpl) {
                connectThread = null
            }

            connected(socket!!, device)
        }

        fun cancel() {
            try {
                //socket?.close()
            } catch (e: IOException) {
                logw("close() of connected socket failed ${e.stackTraceToString()}")
            }
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream
        private val ouputStream: OutputStream

        init {
            inputStream = socket.inputStream
            ouputStream = socket.outputStream

            mutableConnectionState.update {
                BluetoothConnection.Connected
            }
        }

        override fun run() {
            val buffer = ByteArray(1024)

            while (mutableConnectionState.value == BluetoothConnection.Connected) {
                try {
                    inputStream.read(buffer)
                    mutableMessageFlow.update {
                        String(buffer)
                    }
                } catch (e: IOException) {
                    logw("disconnected ${e.stackTraceToString()}")
                    connectionLost()
                    break
                }
            }
        }

        fun write(byteArray: ByteArray) {
            try {
                ouputStream.write(byteArray)
            } catch (e: IOException) {
                logw("write() exception ${e.stackTraceToString()}")
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                logw("close() of connected socket failed ${e.stackTraceToString()}")
            }
        }
    }
}