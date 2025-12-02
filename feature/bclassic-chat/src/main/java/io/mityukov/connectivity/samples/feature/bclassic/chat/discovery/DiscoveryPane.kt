package io.mityukov.connectivity.samples.feature.bclassic.chat.discovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPermissionChecker
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.core.connectivity.bclassic.DiscoveredDevice
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusEvent
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusPane
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusState
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusViewModel
import io.mityukov.connectivity.samples.core.common.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoveryPane(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val viewModel: DiscoveryViewModel = hiltViewModel()
    val viewModelState by viewModel.stateFlow.collectAsStateWithLifecycle()

    val statusViewModel: StatusViewModel = hiltViewModel()
    val statusViewModelState by statusViewModel.stateFlow.collectAsStateWithLifecycle()

    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(windowInfo.isWindowFocused) {
        if (windowInfo.isWindowFocused) {
            statusViewModel.add(StatusEvent.Check)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Обнаружение")
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
        if ((statusViewModelState as? StatusState.Status)?.status == BluetoothStatus.Ok) {
            DiscoveryPaneContent(
                modifier = Modifier.padding(innerPadding),
                viewModelState = viewModelState,
                onFirstDiscovery = {
                    viewModel.add(DiscoveryEvent.StartFirstDiscovery)
                },
                onRefresh = {
                    viewModel.add(DiscoveryEvent.RefreshDiscovery)
                },
                onCancel = {
                    viewModel.add(DiscoveryEvent.StopDiscovery)
                },
                onDeviceSelect = { device ->

                }
            )
        } else {
            StatusPane(
                modifier = Modifier.padding(innerPadding),
                viewModelState = statusViewModelState,
                regularPermissions = BluetoothPermissionChecker.regularRuntimePermissions,
                extraPermissions = BluetoothPermissionChecker.extraRuntimePermissions,
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DiscoveryPaneContent(
    modifier: Modifier,
    viewModelState: DiscoveryState,
    onFirstDiscovery: () -> Unit,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
    onDeviceSelect: (DiscoveredDevice) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                onFirstDiscovery()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (viewModelState) {
        DiscoveryState.Pending -> {
            CircularProgressIndicator()
        }

        is DiscoveryState.Failure -> {
            Text("Не удалось обнаружить устройства поблизости")
        }

        is DiscoveryState.Devices -> {
            Column(
                modifier = modifier.fillMaxWidth()
            ) {
                if (viewModelState.inProgress) {
                    Row {
                        CircularProgressIndicator()
                        Button(onClick = onCancel) {
                            Text("Отменить")
                        }
                    }
                } else {
                    Button(onClick = onRefresh) {
                        Text("Обновить")
                    }
                }
                if (viewModelState.devices.isEmpty()) {
                    Text("Не удалось обнаружить устройства поблизости")
                } else {
                    LazyColumn {
                        items(
                            items = viewModelState.devices,
                            key = { it.name + it.address }) { device ->
                            DeviceItem(
                                device = device,
                                onClick = {
                                    onDeviceSelect(device)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: DiscoveredDevice, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(enabled = true, onClick = onClick)) {
        Text(device.name ?: "NoName")
        Text(device.address)
    }
}