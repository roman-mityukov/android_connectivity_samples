package io.mityukov.connectivity.samples.feature.bclassic.chat

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.mityukov.connectivity.samples.core.connectivity.bclassic.DiscoveredDevice
import io.mityukov.connectivity.samples.feature.bclassic.chat.conversation.ConversationPane
import io.mityukov.connectivity.samples.feature.bclassic.chat.conversation.ConversationViewModel
import io.mityukov.connectivity.samples.feature.bclassic.chat.discovery.DiscoveryPane
import io.mityukov.connectivity.samples.feature.bclassic.chat.paired.PairedDevicesPane

@Composable
fun BluetoothClassicChatHost(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val backStack = rememberNavBackStack(PairedDevicesRoute)
    val pairingCandidateStore = rememberPairingCandidateStore()

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size == 1) {
                onBack()
            } else {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(removeViewModelStoreOnPop = { true }),
        ),
        entryProvider = entryProvider {
            entry<PairedDevicesRoute> {
                val pairingCandidate = pairingCandidateStore.candidate.value
                PairedDevicesPane(
                    snackbarHostState = snackbarHostState,
                    pairingCandidate = pairingCandidate,
                    onBack = onBack,
                    onDiscovery = {
                        backStack.add(DiscoveryRoute)
                    },
                    onDeviceSelected = { pairedDevice ->
                        backStack.add(ConversationRoute(pairedDevice))
                    }
                )
            }
            entry<DiscoveryRoute> {
                DiscoveryPane(
                    snackbarHostState = snackbarHostState,
                    onDeviceSelect = { discoveredDevice ->
                        pairingCandidateStore.candidate.value = discoveredDevice
                        backStack.removeLastOrNull()
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<ConversationRoute> { conversationRoute ->
                ConversationPane(
                    snackbarHostState = snackbarHostState,
                    pairedDevice = conversationRoute.pairedDevice,
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}

@Composable
private fun rememberPairingCandidateStore(): PairingCandidateStore {
    return rememberSaveable(
        saver = Saver(
            save = { it.candidate },
            restore = { PairingCandidateStore().apply { candidate = it } }
        ),
    ) {
        PairingCandidateStore()
    }
}

internal class PairingCandidateStore {
    var candidate = mutableStateOf<DiscoveredDevice?>(null)
}

internal object LocalPairingCandidateStore {
    private val LocalPairingCandidateStore: ProvidableCompositionLocal<PairingCandidateStore?> =
        compositionLocalOf { null }

    val current: PairingCandidateStore
        @Composable
        get() = LocalPairingCandidateStore.current
            ?: error("No PairingCandidateStore has been provided")

    infix fun provides(
        store: PairingCandidateStore
    ): ProvidedValue<PairingCandidateStore?> {
        return LocalPairingCandidateStore.provides(store)
    }
}