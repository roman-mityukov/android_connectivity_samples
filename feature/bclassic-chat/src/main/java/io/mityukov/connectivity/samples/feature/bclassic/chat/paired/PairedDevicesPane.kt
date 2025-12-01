package io.mityukov.connectivity.samples.feature.bclassic.chat.paired

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPermissionChecker
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice
import io.mityukov.connectivity.samples.feature.bclassic.chat.R
import io.mityukov.connectivity.samples.core.common.R as CommonR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PairedDevicesPane(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onDiscovery: () -> Unit,
    onDeviceSelected: () -> Unit,
) {
    val viewModel: PairedDevicesViewModel = hiltViewModel()
    val viewModelState by viewModel.stateFlow.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                viewModel.add(PairedDevicesEvent.GetPairedDevices)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                extraPermissions = BluetoothPermissionChecker.extraRuntimePermissions,
                regularPermissions = BluetoothPermissionChecker.regularRuntimePermissions,
                onDeviceSelected = onDeviceSelected,
                onGetPairedDevices = {
                    viewModel.add(PairedDevicesEvent.GetPairedDevices)
                }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PairedDevicesContent(
    modifier: Modifier,
    viewModelState: PairedDevicesState,
    regularPermissions: List<String>,
    extraPermissions: List<String>,
    onGetPairedDevices: () -> Unit,
    onDeviceSelected: () -> Unit,
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    val regularMultiplePermissionsState =
        rememberMultiplePermissionsState(permissions = regularPermissions)
    val extraMultiplePermissionsState =
        rememberMultiplePermissionsState(permissions = extraPermissions)

    when (viewModelState) {
        is PairedDevicesState.Failure -> {
            when (viewModelState.status) {
                BluetoothStatus.NotSupported -> {
                    Text("Это устройство не поддерживает Bluetooth")
                }

                BluetoothStatus.LocationNotSupported -> {
                    Text("Это устройство не поддерживает геолокацию")
                }

                BluetoothStatus.Disabled -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Bluetooth отключен")
                        Button(onClick = {
                            launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }) {
                            Text("Включить")
                        }
                    }
                }

                BluetoothStatus.LocationDisabled -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Для работы Bluetooth нужно включить геолокацию")
                        Button(onClick = {
                            launcher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }) {
                            Text("Включить")
                        }
                    }
                }

                BluetoothStatus.RegularPermissionsNotGranted -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Чтобы связаться с другим устройством по Bluetooth нужно предоставить разрешения")
                        Button(onClick = {
                            regularMultiplePermissionsState.launchMultiplePermissionRequest()
                        }) {
                            Text("Предоставить")
                        }
                    }
                }

                BluetoothStatus.ExtraPermissionsNotGranted -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Чтобы связаться с другим устройством по Bluetooth дополнительно нужно разрешить использование геолокации в фоне")
                        Button(onClick = {
                            extraMultiplePermissionsState.launchMultiplePermissionRequest()
                        }) {
                            Text("Разрешить")
                        }
                    }
                }

                BluetoothStatus.Ok -> {
                    // no op
                }
            }
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
                LazyColumn {
                    items(items = pairedDevices.toList()) { device ->
                        DeviceItem(device = device, onClick = onDeviceSelected)
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