package io.mityukov.connectivity.samples.feature.bclassic.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothConnectionService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConversationState {
    data object PendingConnection : ConversationState
}

sealed interface ConversationEvent {
    data object Connect : ConversationEvent
}

@HiltViewModel(assistedFactory = ConversationViewModel.Factory::class)
class ConversationViewModel @AssistedInject constructor(
    @Assisted val pairedDevice: PairedDevice,
    private val bluetoothConnectionService: BluetoothConnectionService,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: PairedDevice): ConversationViewModel
    }

    private val mutableStateFlow =
        MutableStateFlow<ConversationState>(ConversationState.PendingConnection)
    val stateFlow = mutableStateFlow.asStateFlow()

    fun add(event: ConversationEvent) {
        when (event) {
            ConversationEvent.Connect -> {
                viewModelScope.launch {
                    bluetoothConnectionService.connect(pairedDevice)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothConnectionService.cancel()
    }
}