package io.mityukov.connectivity.samples.feature.bclassic.chat

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ConversationPane(
    onBack: () -> Unit,
) {
    val viewModel: ConversationViewModel = hiltViewModel()
}