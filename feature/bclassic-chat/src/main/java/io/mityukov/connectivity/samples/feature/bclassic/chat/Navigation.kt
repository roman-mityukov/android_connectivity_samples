package io.mityukov.connectivity.samples.feature.bclassic.chat

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object BluetoothClassicChatRoute: NavKey

@Serializable
data object PairedDevicesRoute : NavKey
@Serializable
data object DiscoveryRoute : NavKey
@Serializable
data object ConversationRoute : NavKey