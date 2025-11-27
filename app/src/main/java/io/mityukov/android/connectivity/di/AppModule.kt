package io.mityukov.android.connectivity.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mityukov.connectivity.samples.core.common.LogsDirectory
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    companion object {
        @Provides
        @LogsDirectory
        fun provideLogsDirectory(@ApplicationContext context: Context): File {
            val logsDirectory = File(context.getExternalFilesDir(null), "logs")

            if (logsDirectory.exists().not()) {
                val isDirectoryCreated = logsDirectory.mkdir()
                if (isDirectoryCreated.not()) {
                    error("Can not create directory with name logs")
                }
            }
            return logsDirectory
        }
    }
}