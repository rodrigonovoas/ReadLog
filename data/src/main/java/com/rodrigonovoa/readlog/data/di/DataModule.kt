package com.rodrigonovoa.readlog.data.di

import com.rodrigonovoa.readlog.data.repository.AuthRepositoryImpl
import com.rodrigonovoa.readlog.data.repository.BookRepositoryImpl
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        impl: BookRepositoryImpl
    ): BookRepository
}
