package io.mityukov.connectivity.samples.feature.bclassic.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothConnection
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothConnectionService
import io.mityukov.connectivity.samples.core.log.logd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ConversationState {
    data object PendingConnection : ConversationState
    data class Messages(val messages: List<String>) : ConversationState
}

sealed interface ConversationEvent {
    data class Send(val message: String) : ConversationEvent
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val connectionService: BluetoothConnectionService,
) : ViewModel() {

    private val mutableStateFlow =
        MutableStateFlow<ConversationState>(ConversationState.PendingConnection)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        logd(connectionService.toString())
        viewModelScope.launch {
            connectionService.connectionState.collect { bluetoothConnection ->
                if (bluetoothConnection == BluetoothConnection.Connected) {
                    mutableStateFlow.update {
                        ConversationState.Messages(listOf())
                    }
                }
            }
        }
        viewModelScope.launch {
            connectionService.message.collect { message ->
                this@ConversationViewModel.logd("message collected $message")
                mutableStateFlow.update {
                    ConversationState.Messages(listOf(message))
                }
            }
        }
    }

    fun add(event: ConversationEvent) {
        when (event) {
            is ConversationEvent.Send -> {
                viewModelScope.launch {
                    connectionService.send(event.message)
                }
            }
        }
    }
}