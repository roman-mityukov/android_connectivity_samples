package io.mityukov.connectivity.samples.feature.bclassic.chat

import androidx.navigation3.runtime.NavKey
import io.mityukov.connectivity.samples.core.connectivity.bclassic.PairedDevice
import kotlinx.serialization.Serializable

@Serializable
data object BluetoothClassicChatRoute: NavKey

@Serializable
data object PairedDevicesRoute : NavKey
@Serializable
data object DiscoveryRoute : NavKey
@Serializable
data class ConversationRoute(val pairedDevice: PairedDevice) : NavKey