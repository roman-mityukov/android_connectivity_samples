package io.mityukov.connectivity.samples.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mityukov.connectivity.samples.core.common.DispatcherDefault
import io.mityukov.connectivity.samples.core.common.DispatcherDefaultLimitedParallelism
import io.mityukov.connectivity.samples.core.common.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @DispatcherIO
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    @DispatcherDefaultLimitedParallelism
    fun providesDispatcherDefaultLimitedParallelism(): CoroutineDispatcher =
        Dispatchers.Default.limitedParallelism(parallelism = 1)

    @Provides
    @DispatcherDefault
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
