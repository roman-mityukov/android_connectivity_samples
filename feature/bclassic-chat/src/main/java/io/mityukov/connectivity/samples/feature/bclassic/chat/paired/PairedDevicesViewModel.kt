package io.mityukov.connectivity.samples.feature.bclassic.chat.paired

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPairedDevicesService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.core.connectivity.bclassic.DiscoveredDevice
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevicesResult
import io.mityukov.connectivity.samples.core.log.logd
import io.mityukov.connectivity.samples.feature.bclassic.chat.paired.PairedDevicesState.Failure
import io.mityukov.connectivity.samples.feature.bclassic.chat.paired.PairedDevicesState.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal sealed interface PairedDevicesState {
    data object Pending : PairedDevicesState
    data class Failure(val status: BluetoothStatus) : PairedDevicesState
    data class Success(val devices: List<PairedDevice>) : PairedDevicesState
}

internal sealed interface PairedDevicesEvent {
    data object GetPairedDevices : PairedDevicesEvent
    data class NewPairingCandidate(val candidate: DiscoveredDevice) : PairedDevicesEvent
}

@HiltViewModel
internal class PairedDevicesViewModel @Inject constructor(
    private val pairedDevicesService: BluetoothPairedDevicesService,
) : ViewModel() {
    private val mutableStateFlow = MutableStateFlow<PairedDevicesState>(PairedDevicesState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    fun add(event: PairedDevicesEvent) {
        when (event) {
            PairedDevicesEvent.GetPairedDevices -> {
                viewModelScope.launch {
                    val state = when (val result = pairedDevicesService.getPairedDevices()) {
                        is PairedDevicesResult.Failure -> Failure(result.status)
                        is PairedDevicesResult.Success -> Success(result.devices)
                    }
                    mutableStateFlow.update {
                        state
                    }
                }
            }

            is PairedDevicesEvent.NewPairingCandidate -> {
                viewModelScope.launch {
                    pairedDevicesService.startPairing(event.candidate)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        logd("onCleared")
    }
}