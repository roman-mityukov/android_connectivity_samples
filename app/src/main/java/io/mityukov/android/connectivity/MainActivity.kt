package io.mityukov.android.connectivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.mityukov.android.connectivity.app.AppNavHost
import io.mityukov.android.connectivity.ui.theme.TestСonnectivityTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestСonnectivityTheme {
                AppNavHost()
            }
        }
    }
}
