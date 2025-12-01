@file:SuppressLint("MissingPermission")

package io.mityukov.connectivity.samples.core.connectivity.bclassic

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mityukov.connectivity.samples.core.log.logd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class BluetoothDiscoveryServiceImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val permissionChecker: BluetoothPermissionChecker,
) : BluetoothDiscoveryService {
    private val mutableStateFlow = MutableStateFlow(
        DiscoveryState(
            progress = DiscoveryProgress.Finished,
            pairedDevices = setOf(),
            discoveredDevices = setOf(),
        )
    )

    override val discoveryFlow = mutableStateFlow
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val discoveryReceiver = DiscoveryBroadcastReceiver()
    private val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter!!

    override fun clear() {
        discoveryReceiver.unregister()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        coroutineScope.cancel()
    }

    override fun ensureDiscoverable() {
        if (bluetoothAdapter.isEnabled && permissionChecker.regularRuntimePermissionsGranted) {
            if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                logd("Ensure discoverable")
                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                applicationContext.startActivity(discoverableIntent)
            } else {
                logd("Already discoverable")
            }
        } else {
            logd("Bluetooth is disabled or permissions are not granted")
        }
    }

    override fun discover() {
        if (bluetoothAdapter.isEnabled && permissionChecker.regularRuntimePermissionsGranted) {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            discoveryReceiver.register()
            val pairedDevices = bluetoothAdapter.bondedDevices
            coroutineScope.launch {
                val currentState = discoveryFlow.first()
                discoveryFlow.update {
                    currentState.copy(
                        pairedDevices = pairedDevices,
                        progress = DiscoveryProgress.Finished,
                        discoveredDevices = setOf(),
                    )
                }
            }

            val startStatus = bluetoothAdapter.startDiscovery()
            this@BluetoothDiscoveryServiceImpl.logd("Start discovery status $startStatus")
        } else {
            logd("Bluetooth is disabled or permissions are not granted")
        }
    }

//    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    private var socket: BluetoothSocket? = null
//
//    suspend fun pair(device: BluetoothDevice) = withContext(Dispatchers.IO) {
//        val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
//        val bluetoothAdapter = bluetoothManager.adapter
//        bluetoothAdapter?.cancelDiscovery()
//
//        val bound = device.createBond()
//        this@BluetoothClassicChatServiceImpl.logd("Create bond result $bound")
//    }
//
//    suspend fun connect(device: BluetoothDevice) = withContext(Dispatchers.IO) {
//        val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
//        val bluetoothAdapter = bluetoothManager.adapter
//        bluetoothAdapter?.cancelDiscovery()
//
//        socket = device.createRfcommSocketToServiceRecord(uuid)
//        socket?.connect()
//    }

    inner class DiscoveryBroadcastReceiver : BroadcastReceiver() {
        private var isRegistered: Boolean = false

        @Synchronized
        fun register() {
            logd("register")
            if (isRegistered.not()) {
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

                ContextCompat.registerReceiver(
                    applicationContext,
                    this,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED, // Bluetooth broadcasts are not system broadcasts
                )
                isRegistered = true
            }
        }

        @Synchronized
        fun unregister() {
            logd("unregister")
            if (isRegistered) {
                applicationContext.unregisterReceiver(this)
                isRegistered = false
            }
        }

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    logd("onReceive BluetoothAdapter.ACTION_DISCOVERY_STARTED")
                    coroutineScope.launch {
                        val currentState = discoveryFlow.first()
                        discoveryFlow.update {
                            currentState.copy(progress = DiscoveryProgress.Started)
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    logd("onReceive BluetoothAdapter.ACTION_DISCOVERY_FINISHED")
                    coroutineScope.launch {
                        val currentState = discoveryFlow.first()
                        discoveryFlow.update {
                            currentState.copy(progress = DiscoveryProgress.Finished)
                        }
                    }
                }

                BluetoothDevice.ACTION_FOUND -> {
                    logd("onReceive BluetoothAdapter.ACTION_FOUND")
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address
                    logd("Discovered device $deviceName $deviceHardwareAddress")

                    coroutineScope.launch {
                        val currentState = discoveryFlow.first()

                        val newDiscoveredDevices = mutableSetOf<BluetoothDevice>()
                        newDiscoveredDevices.addAll(currentState.discoveredDevices)
                        newDiscoveredDevices.add(device)

                        discoveryFlow.update {
                            currentState.copy(discoveredDevices = newDiscoveredDevices)
                        }
                    }
                }

//                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
//                    val device: BluetoothDevice =
//                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
//                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
//                    val previousBondState =
//                        intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
//
//                    this@BluetoothClassicChatServiceImpl.logd("bondState $bondState previousBondState $previousBondState")
//
//                    when (bondState) {
//                        BluetoothDevice.BOND_BONDING ->                             // Устройство в процессе pairing
//                            this@BluetoothClassicChatServiceImpl.logd("Bonding with device: " + device.getName())
//
//                        BluetoothDevice.BOND_BONDED ->                             // Устройство успешно сопряжено
//                            this@BluetoothClassicChatServiceImpl.logd("Bonded with device: " + device.getName())
//
//                        BluetoothDevice.BOND_NONE ->                             // Сопряжение отменено или разорвано
//                            this@BluetoothClassicChatServiceImpl.logd("Not bonded with device: " + device.getName())
//                    }
//                }
            }
        }
    }
}