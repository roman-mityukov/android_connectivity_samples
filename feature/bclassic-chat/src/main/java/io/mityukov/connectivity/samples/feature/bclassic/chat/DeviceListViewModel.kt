package io.mityukov.connectivity.samples.feature.bclassic.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothClassicChatService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothHealthService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothHealthState
import io.mityukov.connectivity.samples.core.connectivity.bclassic.DiscoveryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal sealed interface DeviceListState {
    data object Pending : DeviceListState
    data class BluetoothHealth(val state: BluetoothHealthState) : DeviceListState
    data class Discovery(val state: DiscoveryState) : DeviceListState
}

internal sealed interface DeviceListEvent {
    data object CheckBluetoothHealth : DeviceListEvent
    data object EnsureDiscoverable : DeviceListEvent
    data object Discover : DeviceListEvent
}

@HiltViewModel
internal class DeviceListViewModel @Inject constructor(
    private val bluetoothHealthService: BluetoothHealthService,
    private val chatService: BluetoothClassicChatService,
) : ViewModel() {
    private val mutableHealthStateFlow =
        MutableStateFlow(bluetoothHealthService.bluetoothHealth)
    val stateFlow = mutableHealthStateFlow.combine(chatService.discoveryFlow) { health, discovery ->
        if (health == BluetoothHealthState.Ok) {
            DeviceListState.Discovery(state = discovery)
        } else {
            DeviceListState.BluetoothHealth(state = health)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = DeviceListState.Pending
    )

    fun add(event: DeviceListEvent) {
        when (event) {
            DeviceListEvent.CheckBluetoothHealth -> {
                mutableHealthStateFlow.update {
                    bluetoothHealthService.bluetoothHealth
                }
            }

            DeviceListEvent.EnsureDiscoverable -> {
                chatService.ensureDiscoverable()
            }

            DeviceListEvent.Discover -> {
                chatService.discover()
            }
        }
    }
}