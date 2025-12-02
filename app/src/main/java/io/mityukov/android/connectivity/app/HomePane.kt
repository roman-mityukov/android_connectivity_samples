package io.mityukov.android.connectivity.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.mityukov.connectivity.samples.core.log.Logger.logd

@Composable
fun HomePane(
    onBluetoothSelected: () -> Unit,
    onUsbSelected: () -> Unit,
) {
    logd("LocalViewModelStoreOwner.current ${LocalViewModelStoreOwner.current}")

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)
            ) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = onBluetoothSelected) {
                    Text(text = "Bluetooth")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = onUsbSelected) {
                    Text(text = "USB")
                }
            }
        }
    }
}