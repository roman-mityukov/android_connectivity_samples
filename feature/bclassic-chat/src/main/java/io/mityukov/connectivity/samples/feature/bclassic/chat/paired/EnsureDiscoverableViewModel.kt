package io.mityukov.connectivity.samples.feature.bclassic.chat.paired

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPairedDevicesService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.core.connectivity.bclassic.EnsureDiscoverableResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EnsureDiscoverableState {
    data object Default : EnsureDiscoverableState
    data object AlreadyDiscoverable : EnsureDiscoverableState
    data class Failure(val status: BluetoothStatus) : EnsureDiscoverableState
}

sealed interface EnsureDiscoverableEvent {
    data object Ensure : EnsureDiscoverableEvent
    data object ConsumeFailure : EnsureDiscoverableEvent
}

@HiltViewModel
class EnsureDiscoverableViewModel @Inject constructor(
    private val pairedDevicesService: BluetoothPairedDevicesService
) : ViewModel() {
    private val mutableStateFlow =
        MutableStateFlow<EnsureDiscoverableState>(EnsureDiscoverableState.Default)
    val stateFlow = mutableStateFlow.asStateFlow()

    fun add(event: EnsureDiscoverableEvent) {
        when (event) {
            EnsureDiscoverableEvent.ConsumeFailure -> {
                mutableStateFlow.update {
                    EnsureDiscoverableState.Default
                }
            }

            EnsureDiscoverableEvent.Ensure -> {
                viewModelScope.launch {
                    val state = when (val result = pairedDevicesService.ensureDiscoverable()) {
                        is EnsureDiscoverableResult.Failure -> EnsureDiscoverableState.Failure(
                            result.status
                        )

                        EnsureDiscoverableResult.Success -> EnsureDiscoverableState.Default
                        EnsureDiscoverableResult.AlreadyDiscoverable -> EnsureDiscoverableState.AlreadyDiscoverable
                    }
                    mutableStateFlow.update { state }
                }
            }
        }
    }
}