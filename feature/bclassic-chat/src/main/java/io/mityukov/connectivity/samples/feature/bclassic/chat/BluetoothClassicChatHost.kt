package io.mityukov.connectivity.samples.feature.bclassic.chat

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.mityukov.connectivity.samples.feature.bclassic.chat.discovery.DiscoveryPane
import io.mityukov.connectivity.samples.feature.bclassic.chat.paired.PairedDevicesPane

@Composable
fun BluetoothClassicChatHost(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val backStack = rememberNavBackStack(PairedDevicesRoute)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else {
                onBack()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<PairedDevicesRoute> {
                PairedDevicesPane(
                    snackbarHostState = snackbarHostState,
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    onDiscovery = {
                        backStack.add(DiscoveryRoute)
                    },
                    onDeviceSelected = {
                        backStack.add(ConversationRoute)
                    }
                )
            }
            entry<DiscoveryRoute> {
                DiscoveryPane(
                    snackbarHostState = snackbarHostState,
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<ConversationRoute> {
                ConversationPane(
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}