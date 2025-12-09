package io.mityukov.connectivity.samples.feature.bclassic.chat.conversation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice
import io.mityukov.connectivity.samples.core.common.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationPane(
    snackbarHostState: SnackbarHostState,
    pairedDevice: PairedDevice,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Чат")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CommonR.string.core_common_content_description_button_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeContent
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column {
                ConnectionPane(pairedDevice = pairedDevice)
                ConversationPaneContent()
            }
        }
    }
}

@Composable
private fun ConversationPaneContent(viewModel: ConversationViewModel = hiltViewModel()) {
    val messageText = remember { mutableStateOf("") }

    val viewModelState by viewModel.stateFlow.collectAsStateWithLifecycle()
    when (viewModelState) {
        is ConversationState.Messages -> {
            Column {
                Column(modifier = Modifier.weight(1f)) {
                    (viewModelState as ConversationState.Messages).messages.forEach { message ->
                        Text(message)
                    }
                }
                Row {
                    OutlinedTextField(
                        value = messageText.value,
                        onValueChange = { value -> messageText.value = value },
                    )
                    Button(
                        enabled = true,
                        onClick = {
                            viewModel.add(ConversationEvent.Send(messageText.value))
                        },
                    ) {
                        Text("Отправить")
                    }
                }
            }
        }

        ConversationState.PendingConnection -> {
            CircularProgressIndicator()
        }
    }
}