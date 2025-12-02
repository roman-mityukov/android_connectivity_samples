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
import io.mityukov.connectivity.samples.core.common.DispatcherIO
import io.mityukov.connectivity.samples.core.log.logd
import io.mityukov.connectivity.samples.core.log.logw
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BluetoothPairedDevicesServiceImpl @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val statusService: BluetoothStatusService,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
) : BluetoothPairedDevicesService {
    private val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter!!

    override suspend fun getPairedDevices(): PairedDevicesResult = withContext(dispatcher) {
        this@BluetoothPairedDevicesServiceImpl.logd("getPairedDevices")
        when (val status = statusService.status) {
            BluetoothStatus.Ok -> PairedDevicesResult.Success(
                bluetoothAdapter.bondedDevices.map {
                    PairedDevice(name = it.name, address = it.address)
                },
            )

            else -> {
                this@BluetoothPairedDevicesServiceImpl.logw("Bluetooth error $status")
                PairedDevicesResult.Failure(status = status)
            }
        }
    }

    override suspend fun ensureDiscoverable(): EnsureDiscoverableResult = withContext(dispatcher) {
        this@BluetoothPairedDevicesServiceImpl.logd("ensureDiscoverable")
        when (val status = statusService.status) {
            BluetoothStatus.Ok -> {
                if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                    discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    applicationContext.startActivity(discoverableIntent)
                    EnsureDiscoverableResult.Success
                } else {
                    this@BluetoothPairedDevicesServiceImpl.logd("Already discoverable")
                    EnsureDiscoverableResult.AlreadyDiscoverable
                }
            }

            else -> {
                this@BluetoothPairedDevicesServiceImpl.logw("Bluetooth error $status")
                EnsureDiscoverableResult.Failure(status)
            }
        }
    }

    override suspend fun startPairing(device: DiscoveredDevice): StartPairingResult =
        withContext(dispatcher) {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.address)
            val result = bluetoothDevice.createBond()
            this@BluetoothPairedDevicesServiceImpl.logd("Create bond result $result")
            if (result) {
                StartPairingResult.Success
            } else {
                StartPairingResult.Failure
            }
        }

    inner class PairingBroadcastReceiver : BroadcastReceiver() {
        private var isRegistered: Boolean = false

        @Synchronized
        fun register() {
            logd("register")
            if (isRegistered.not()) {
                val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

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
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val previousBondState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                    this@BluetoothPairedDevicesServiceImpl.logd("bondState $bondState previousBondState $previousBondState")

                    when (bondState) {
                        BluetoothDevice.BOND_BONDING ->                             // Устройство в процессе pairing
                            this@BluetoothPairedDevicesServiceImpl.logd("Bonding with device: " + device.getName())

                        BluetoothDevice.BOND_BONDED ->                             // Устройство успешно сопряжено
                            this@BluetoothPairedDevicesServiceImpl.logd("Bonded with device: " + device.getName())

                        BluetoothDevice.BOND_NONE ->                             // Сопряжение отменено или разорвано
                            this@BluetoothPairedDevicesServiceImpl.logd("Not bonded with device: " + device.getName())
                    }
                }
            }
        }
    }
}