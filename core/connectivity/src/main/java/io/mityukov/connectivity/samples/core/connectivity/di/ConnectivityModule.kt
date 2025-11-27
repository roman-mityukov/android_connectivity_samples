package io.mityukov.connectivity.samples.core.connectivity.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothClassicChatService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothClassicChatServiceImpl
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothHealthService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothHealthServiceImpl
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPermissionChecker
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPermissionCheckerImpl

@Module
@InstallIn(SingletonComponent::class)
interface ConnectivityModule {
    @Binds
    fun bindsBluetoothPermissionChecker(impl: BluetoothPermissionCheckerImpl): BluetoothPermissionChecker

    @Binds
    fun bindsBluetoothHealthService(impl: BluetoothHealthServiceImpl): BluetoothHealthService

    @Binds
    fun bindsBluetoothClassicChatService(impl: BluetoothClassicChatServiceImpl): BluetoothClassicChatService
}