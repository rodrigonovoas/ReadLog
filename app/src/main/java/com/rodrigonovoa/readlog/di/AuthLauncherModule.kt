package com.rodrigonovoa.readlog.di

import android.content.Context
import com.rodrigonovoa.readlog.auth.GoogleAuthLauncher
import com.rodrigonovoa.readlog.domain.auth.AuthLauncher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthLauncherModule {

    @Provides
    @Singleton
    fun provideAuthLauncher(
        @ApplicationContext context: Context
    ): AuthLauncher = GoogleAuthLauncher(context)
}
