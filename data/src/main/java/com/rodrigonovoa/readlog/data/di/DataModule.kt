package com.rodrigonovoa.readlog.data.di

import com.rodrigonovoa.readlog.data.mapper.AnnotationDataMapper
import com.rodrigonovoa.readlog.data.mapper.AnnotationDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.BookDataMapper
import com.rodrigonovoa.readlog.data.mapper.BookDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.SessionDataMapper
import com.rodrigonovoa.readlog.data.mapper.SessionDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.UserDataMapper
import com.rodrigonovoa.readlog.data.mapper.UserDataMapperImpl
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

    @Binds
    abstract fun bindUserDataMapper(
        impl: UserDataMapperImpl
    ): UserDataMapper

    @Binds
    abstract fun bindBookDataMapper(
        impl: BookDataMapperImpl
    ): BookDataMapper

    @Binds
    abstract fun bindSessionDataMapper(
        impl: SessionDataMapperImpl
    ): SessionDataMapper

    @Binds
    abstract fun bindAnnotationDataMapper(
        impl: AnnotationDataMapperImpl
    ): AnnotationDataMapper
}
