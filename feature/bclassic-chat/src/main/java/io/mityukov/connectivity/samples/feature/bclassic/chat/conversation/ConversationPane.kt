package io.mityukov.connectivity.samples.feature.bclassic.chat.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ConversationPane(
    viewModel: ConversationViewModel,
    onBack: () -> Unit,
) {
    LaunchedEffect(viewModel.pairedDevice) {
        viewModel.add(ConversationEvent.Connect)
    }
}