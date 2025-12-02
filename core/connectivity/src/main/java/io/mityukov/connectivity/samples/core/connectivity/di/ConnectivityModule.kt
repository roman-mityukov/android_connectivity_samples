package io.mityukov.connectivity.samples.core.connectivity.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothDiscoveryService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothDiscoveryServiceImpl
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPairedDevicesService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPairedDevicesServiceImpl
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatusService
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothStatusServiceImpl
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPermissionChecker
import io.mityukov.connectivity.samples.core.connectivity.bclassic.BluetoothPermissionCheckerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {
    @Binds
    internal abstract fun bindsBluetoothPermissionChecker(impl: BluetoothPermissionCheckerImpl): BluetoothPermissionChecker

    @Binds
    internal abstract fun bindsBluetoothStatusService(impl: BluetoothStatusServiceImpl): BluetoothStatusService

    @Binds
    internal abstract fun bindsBluetoothPairedDevicesService(impl: BluetoothPairedDevicesServiceImpl): BluetoothPairedDevicesService

    @Binds
    internal abstract fun bindsBluetoothDiscoveryService(impl: BluetoothDiscoveryServiceImpl): BluetoothDiscoveryService
}