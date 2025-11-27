package io.mityukov.android.connectivity.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.mityukov.connectivity.samples.core.common.LogsDirectory
import io.mityukov.connectivity.samples.core.log.Logger
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class TestConnectivityApplication : Application() {
    @Inject
    @LogsDirectory
    lateinit var logsDirectory: File

    override fun onCreate() {
        super.onCreate()
        Logger.initLogs(logsDirectory)
    }
}