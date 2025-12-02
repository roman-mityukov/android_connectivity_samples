package io.mityukov.connectivity.samples.feature.bclassic.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothConnection
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothConnectionService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice
import io.mityukov.connectivity.samples.core.log.logd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    data object None : ConnectionState
    data object Listen : ConnectionState
    data object Connecting : ConnectionState
    data object Connected : ConnectionState
}

sealed interface ConnectionEvent {
    data object Connect : ConnectionEvent
    data object Disconnect : ConnectionEvent
}

@HiltViewModel(assistedFactory = ConnectionViewModel.Factory::class)
class ConnectionViewModel @AssistedInject constructor(
    @Assisted private val pairedDevice: PairedDevice,
    private val connectionService: BluetoothConnectionService
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(device: PairedDevice): ConnectionViewModel
    }

    private val mutableStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.None)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            connectionService.start()
        }
        viewModelScope.launch {
            connectionService.connectionState.collect { connectionState ->
                mutableStateFlow.update {
                    when (connectionState) {
                        BluetoothConnection.Connected -> ConnectionState.Connected
                        BluetoothConnection.Connecting -> ConnectionState.Connecting
                        BluetoothConnection.Listen -> ConnectionState.Listen
                        BluetoothConnection.None -> ConnectionState.None
                    }
                }
            }
        }
    }

    fun add(event: ConnectionEvent) {
        when (event) {
            ConnectionEvent.Connect -> {
                viewModelScope.launch {
                    connectionService.connect(pairedDevice)
                }
            }

            ConnectionEvent.Disconnect -> {
                viewModelScope.launch {
                    connectionService.stop()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectionService.stop()
    }
}