package io.mityukov.connectivity.samples.feature.bclassic.chat

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothHealthState
import io.mityukov.connectivity.samples.core.connectivity.bclassic.DiscoveryProgress
import io.mityukov.connectivity.samples.core.common.R as CommonR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BluetoothClassicDeviceListPane(
    onBack: () -> Unit,
    onDeviceSelected: () -> Unit,
) {
    val viewModel: DeviceListViewModel = hiltViewModel()
    val viewModelState by viewModel.stateFlow.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                viewModel.add(DeviceListEvent.CheckBluetoothHealth)
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
                    Text(text = "Bluetooth")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CommonR.string.core_common_content_description_button_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.add(DeviceListEvent.EnsureDiscoverable)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Announcement,
                            contentDescription = stringResource(R.string.feature_bclassic_chat_content_description_button_ensure_discoverable),
                        )
                    }
                    IconButton(onClick = {
                        viewModel.add(DeviceListEvent.Discover)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.feature_bclassic_chat_content_description_button_discover),
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BluetoothClassicDeviceListContent(viewModelState)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun BluetoothClassicDeviceListContent(viewModelState: DeviceListState) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        }

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    )

    when (viewModelState) {
        is DeviceListState.BluetoothHealth -> {
            when (viewModelState.state) {
                BluetoothHealthState.NotSupported -> {
                    Text("Это устройство не поддерживает Bluetooth")
                }

                BluetoothHealthState.Disabled -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Bluetooth отключен")
                        Button(onClick = {
                            launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }) {
                            Text("Включить")
                        }
                    }
                }

                BluetoothHealthState.PermissionsNotGranted -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Чтобы связаться с другим устройством по Bluetooth нужно предоставить разрешения")
                        Button(onClick = {
                            multiplePermissionsState.launchMultiplePermissionRequest()
                        }) {
                            Text("Предоставить")
                        }
                    }
                }

                BluetoothHealthState.Ok -> {
                    // no op
                }
            }
        }

        is DeviceListState.Discovery -> {
            val pairedDevices = viewModelState.state.pairedDevices
            val discoverableDevices = viewModelState.state.discoveredDevices

            Column {
                Text(if (viewModelState.state.progress == DiscoveryProgress.Started) "Обнаружение устройств..." else "")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Paired")
                LazyColumn {
                    if (pairedDevices.isEmpty()) {
                        items(count = 1) {
                            Text("Нет спаренных устройств...")
                        }
                    } else {
                        items(items = pairedDevices.toList()) { device ->
                            DeviceItem(device = device, onClick = {

                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Discoverable")
                LazyColumn {
                    if (discoverableDevices.isEmpty()) {
                        items(count = 1) {
                            Text("Поиск устройств...")
                        }
                    } else {
                        items(items = discoverableDevices.toList()) { device ->
                            DeviceItem(device = device, onClick = {

                            })
                        }
                    }
                }
            }
        }

        DeviceListState.Pending -> {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun DeviceItem(device: BluetoothDevice, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(enabled = true, onClick = onClick)) {
        Text(device.name ?: "NoName")
        Text(device.address)
        Text(device.bluetoothClass.toString())
    }
}