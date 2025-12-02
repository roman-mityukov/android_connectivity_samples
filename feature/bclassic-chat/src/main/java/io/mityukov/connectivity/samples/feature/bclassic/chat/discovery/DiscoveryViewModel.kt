package io.mityukov.connectivity.samples.feature.bclassic.chat.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothDiscoveryProgress
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothDiscoveryService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.core.connectivity.bclassic.DiscoveredDevice
import io.mityukov.connectivity.samples.core.connectivity.bclassic.StartDiscoveryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DiscoveryState {
    data object Pending : DiscoveryState
    data class Failure(val status: BluetoothStatus) : DiscoveryState
    data class Devices(val inProgress: Boolean, val devices: List<DiscoveredDevice>) :
        DiscoveryState
}

sealed interface DiscoveryEvent {
    data object StartFirstDiscovery: DiscoveryEvent
    data object RefreshDiscovery: DiscoveryEvent
    data object StopDiscovery : DiscoveryEvent
}

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val bluetoothDiscoveryService: BluetoothDiscoveryService
) : ViewModel() {
    private var isFirstDiscovery = true
    private val mutableStateFlow = MutableStateFlow<DiscoveryState>(DiscoveryState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            bluetoothDiscoveryService.discoveryFlow.collect { bluetoothDiscoveryState ->
                mutableStateFlow.update {
                    DiscoveryState.Devices(
                        inProgress = bluetoothDiscoveryState.progress == BluetoothDiscoveryProgress.Started,
                        devices = bluetoothDiscoveryState.discoveredDevices.toList()
                    )
                }
            }
        }
    }

    fun add(event: DiscoveryEvent) {
        when (event) {
            DiscoveryEvent.StartFirstDiscovery -> {
                viewModelScope.launch {
                    if (isFirstDiscovery) {
                        isFirstDiscovery = false
                        bluetoothDiscoveryService.startDiscovery()
                    }
                }
            }

            DiscoveryEvent.RefreshDiscovery -> {
                viewModelScope.launch {
                    bluetoothDiscoveryService.startDiscovery()
                }
            }

            DiscoveryEvent.StopDiscovery -> {
                bluetoothDiscoveryService.stopDiscovery()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothDiscoveryService.clear()
    }
}