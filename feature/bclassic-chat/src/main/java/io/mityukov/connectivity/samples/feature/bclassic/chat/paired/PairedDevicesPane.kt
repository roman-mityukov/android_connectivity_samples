package io.mityukov.connectivity.samples.feature.bclassic.chat.paired

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPermissionChecker
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.core.connectivity.bclassic.DiscoveredDevice
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice
import io.mityukov.connectivity.samples.core.log.Logger.logd
import io.mityukov.connectivity.samples.feature.bclassic.chat.R
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusEvent
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusPane
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusState
import io.mityukov.connectivity.samples.feature.bclassic.chat.status.StatusViewModel
import kotlinx.coroutines.launch
import io.mityukov.connectivity.samples.core.common.R as CommonR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PairedDevicesPane(
    snackbarHostState: SnackbarHostState,
    pairingCandidate: DiscoveredDevice?,
    onBack: () -> Unit,
    onDiscovery: () -> Unit,
    onDeviceSelected: (PairedDevice) -> Unit,
) {
    val viewModel: PairedDevicesViewModel = hiltViewModel()
    val viewModelState by viewModel.stateFlow.collectAsStateWithLifecycle()

    val statusViewModel: StatusViewModel = hiltViewModel()
    val statusViewModelState by statusViewModel.stateFlow.collectAsStateWithLifecycle()

    val windowInfo = LocalWindowInfo.current

    logd("LocalViewModelStoreOwner.current ${LocalViewModelStoreOwner.current}")

    LaunchedEffect(windowInfo.isWindowFocused) {
        if (windowInfo.isWindowFocused) {
            statusViewModel.add(StatusEvent.Check)
        }
    }

    LaunchedEffect(pairingCandidate) {
        if (pairingCandidate != null) {
            viewModel.add(PairedDevicesEvent.NewPairingCandidate(pairingCandidate))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Связанные устройства")
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if ((statusViewModelState as? StatusState.Status)?.status == BluetoothStatus.Ok) {
                Row(horizontalArrangement = Arrangement.Center) {
                    Button(onClick = onDiscovery) {
                        Text(text = stringResource(R.string.feature_bclassic_chat_content_description_button_discover))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    EnsureDiscoverablePane(snackbarHostState = snackbarHostState)
                }
                Spacer(modifier = Modifier.height(16.dp))
                PairedDevicesContent(
                    modifier = Modifier.weight(1f),
                    viewModelState = viewModelState,
                    onDeviceSelected = onDeviceSelected,
                    onGetPairedDevices = {
                        viewModel.add(PairedDevicesEvent.GetPairedDevices)
                    }
                )
            } else {
                StatusPane(
                    viewModelState = statusViewModelState,
                    regularPermissions = BluetoothPermissionChecker.regularRuntimePermissions,
                    extraPermissions = BluetoothPermissionChecker.extraRuntimePermissions,
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PairedDevicesContent(
    modifier: Modifier,
    viewModelState: PairedDevicesState,
    onGetPairedDevices: () -> Unit,
    onDeviceSelected: (PairedDevice) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onGetPairedDevices()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (viewModelState) {
        is PairedDevicesState.Failure -> {

        }

        is PairedDevicesState.Success -> {
            val pairedDevices = viewModelState.devices

            if (pairedDevices.isEmpty()) {
                Column {
                    Text(
                        "Нет связанных устройств, добавьте новое с помощью кнопки “Новое сопряжение”\n" +
                                "\n" +
                                "или\n" +
                                "\n" +
                                "Сделайте Ваше устройство видимым для других с помощью кнопки “Сделать видимым”"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onGetPairedDevices
                    ) {
                        Text("Обновить")
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = {
                        onGetPairedDevices()
                    }
                ) {
                    LazyColumn {
                        items(items = pairedDevices.toList()) { device ->
                            DeviceItem(
                                device = device,
                                onClick = {
                                    onDeviceSelected(device)
                                },
                            )
                        }
                    }
                }
            }
        }

        PairedDevicesState.Pending -> {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun DeviceItem(device: PairedDevice, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(enabled = true, onClick = onClick)) {
        Text(device.name ?: "NoName")
        Text(device.address)
    }
}