package io.mityukov.android.connectivity.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.mityukov.connectivity.samples.feature.bclassic.chat.BluetoothClassicChatHost
import io.mityukov.connectivity.samples.feature.bclassic.chat.BluetoothClassicChatRoute
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

    val snackbarHostState = remember { SnackbarHostState() }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(removeViewModelStoreOnPop = { true }),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomePane(
                    onBluetoothSelected = {
                        backStack.add(BluetoothClassicChatRoute)
                    },
                    onUsbSelected = {}
                )
            }
            entry<BluetoothClassicChatRoute> {
                BluetoothClassicChatHost(
                    snackbarHostState = snackbarHostState,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}