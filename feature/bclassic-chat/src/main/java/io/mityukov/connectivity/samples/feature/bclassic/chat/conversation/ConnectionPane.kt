package io.mityukov.connectivity.samples.feature.bclassic.chat.conversation

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice

@Composable
fun ConnectionPane(modifier: Modifier = Modifier, pairedDevice: PairedDevice) {
    val viewModel: ConnectionViewModel =
        hiltViewModel<ConnectionViewModel, ConnectionViewModel.Factory>(creationCallback = { factory ->
            factory.create(pairedDevice)
        })

    val viewModelState by viewModel.stateFlow.collectAsStateWithLifecycle()

    Row {
        Text(viewModelState.toString())
        Button(
            enabled = viewModelState == ConnectionState.Listen,
            onClick = {
                viewModel.add(ConnectionEvent.Connect)
            },
        ) {
            Text("Связаться")
        }
    }
}