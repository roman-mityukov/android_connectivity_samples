package io.mityukov.connectivity.samples.feature.bclassic.chat.paired

import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.feature.bclassic.chat.R
import kotlinx.coroutines.launch

@Composable
fun EnsureDiscoverablePane(
    modifier: Modifier = Modifier,
    viewModel: EnsureDiscoverableViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
) {
    val viewModelState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val message = when (viewModelState) {
        EnsureDiscoverableState.Default -> null
        EnsureDiscoverableState.AlreadyDiscoverable -> "Это устройство уже видимо"
        is EnsureDiscoverableState.Failure -> {
            val status = (viewModelState as EnsureDiscoverableState.Failure).status
            when (status) {
                BluetoothStatus.Disabled -> "Bluetooth отключен"
                BluetoothStatus.PermissionsNotGranted -> "Нужно предоставить разрешения"
                else -> null
            }
        }
    }

    LaunchedEffect(message) {
        viewModel.add(EnsureDiscoverableEvent.ConsumeFailure)
        message?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Button(onClick = {
        viewModel.add(EnsureDiscoverableEvent.Ensure)
    }) {
        Text(
            text = stringResource(R.string.feature_bclassic_chat_content_description_button_ensure_discoverable),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}