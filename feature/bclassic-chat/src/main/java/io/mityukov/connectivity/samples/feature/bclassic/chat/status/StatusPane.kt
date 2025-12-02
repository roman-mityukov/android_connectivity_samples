package io.mityukov.connectivity.samples.feature.bclassic.chat.status

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatus

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun StatusPane(
    modifier: Modifier = Modifier,
    viewModelState: StatusState,
    regularPermissions: List<String>,
    extraPermissions: List<String>,
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    val regularMultiplePermissionsState =
        rememberMultiplePermissionsState(permissions = regularPermissions)
    val extraMultiplePermissionsState =
        rememberMultiplePermissionsState(permissions = extraPermissions)

    when (viewModelState) {
        StatusState.Pending -> CircularProgressIndicator()
        is StatusState.Status -> {
            Box(modifier = modifier) {
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
        }
    }
}