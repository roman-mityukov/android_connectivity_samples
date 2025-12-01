package io.mityukov.connectivity.samples.feature.bclassic.chat.status

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatusService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal sealed interface StatusState {
    data object Pending : StatusState
    data class Status(val status: BluetoothStatus) : StatusState
}

internal sealed interface StatusEvent {
    data object Check : StatusEvent
}

@HiltViewModel
internal class StatusViewModel @Inject constructor(
    private val bluetoothStatusService: BluetoothStatusService
) : ViewModel() {
    val mutableStateFlow = MutableStateFlow<StatusState>(StatusState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    fun add(event: StatusEvent) {
        when (event) {
            StatusEvent.Check -> {
                mutableStateFlow.update {
                    StatusState.Status(bluetoothStatusService.status)
                }
            }
        }
    }
}