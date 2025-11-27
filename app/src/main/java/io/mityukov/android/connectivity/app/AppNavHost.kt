package io.mityukov.android.connectivity.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.mityukov.connectivity.samples.feature.bclassic.chat.BluetoothClassicChatPane
import io.mityukov.connectivity.samples.feature.bclassic.chat.BluetoothClassicChatRoute
import io.mityukov.connectivity.samples.feature.bclassic.chat.BluetoothClassicDeviceListPane
import io.mityukov.connectivity.samples.feature.bclassic.chat.BluetoothClassicDeviceListRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Composable
fun AppNavHost() {
    val backStack = remember {
        mutableStateListOf<NavKey>(
            HomeRoute
        )
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomePane(
                    onBluetoothSelected = {
                        backStack.add(BluetoothClassicDeviceListRoute)
                    },
                    onUsbSelected = {}
                )
            }
            entry<BluetoothClassicDeviceListRoute> {
                BluetoothClassicDeviceListPane(
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    onDeviceSelected = {
                        backStack.add(BluetoothClassicChatRoute)
                    }
                )
            }
            entry<BluetoothClassicChatRoute> {
                BluetoothClassicChatPane(
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}