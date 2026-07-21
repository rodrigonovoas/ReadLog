package com.rodrigonovoa.readlog.data.di

import com.rodrigonovoa.readlog.data.firestore.AnnotationFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.AnnotationFirestoreDataSourceImpl
import com.rodrigonovoa.readlog.data.firestore.BookFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.BookFirestoreDataSourceImpl
import com.rodrigonovoa.readlog.data.firestore.SessionFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.SessionFirestoreDataSourceImpl
import com.rodrigonovoa.readlog.data.firestore.UserProfileInfoFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.UserProfileInfoFirestoreDataSourceImpl
import com.rodrigonovoa.readlog.data.firestore.UserSearchFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.UserSearchFirestoreDataSourceImpl
import com.rodrigonovoa.readlog.data.mapper.AnnotationDataMapper
import com.rodrigonovoa.readlog.data.mapper.AnnotationDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.AnnotationFirestoreMapper
import com.rodrigonovoa.readlog.data.mapper.AnnotationFirestoreMapperImpl
import com.rodrigonovoa.readlog.data.mapper.BookDataMapper
import com.rodrigonovoa.readlog.data.mapper.BookDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.BookFirestoreMapper
import com.rodrigonovoa.readlog.data.mapper.BookFirestoreMapperImpl
import com.rodrigonovoa.readlog.data.mapper.SessionDataMapper
import com.rodrigonovoa.readlog.data.mapper.SessionDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.SessionFirestoreMapper
import com.rodrigonovoa.readlog.data.mapper.SessionFirestoreMapperImpl
import com.rodrigonovoa.readlog.data.mapper.UserDataMapper
import com.rodrigonovoa.readlog.data.mapper.UserDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.UserProfileInfoDataMapper
import com.rodrigonovoa.readlog.data.mapper.UserProfileInfoDataMapperImpl
import com.rodrigonovoa.readlog.data.mapper.UserProfileInfoFirestoreMapper
import com.rodrigonovoa.readlog.data.mapper.UserProfileInfoFirestoreMapperImpl
import com.rodrigonovoa.readlog.data.mapper.UserSearchFirestoreMapper
import com.rodrigonovoa.readlog.data.mapper.UserSearchFirestoreMapperImpl
import com.rodrigonovoa.readlog.data.repository.AnnotationRepositoryImpl
import com.rodrigonovoa.readlog.data.repository.AuthRepositoryImpl
import com.rodrigonovoa.readlog.data.repository.BookRepositoryImpl
import com.rodrigonovoa.readlog.data.repository.SessionRepositoryImpl
import com.rodrigonovoa.readlog.data.repository.SyncRepositoryImpl
import com.rodrigonovoa.readlog.data.repository.UserProfileRepositoryImpl
import com.rodrigonovoa.readlog.data.repository.UserSearchRepositoryImpl
import com.rodrigonovoa.readlog.domain.repository.AnnotationRepository
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import com.rodrigonovoa.readlog.domain.repository.SyncRepository
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
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
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindAnnotationRepository(
        impl: AnnotationRepositoryImpl
    ): AnnotationRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindUserSearchRepository(
        impl: UserSearchRepositoryImpl
    ): UserSearchRepository

    @Binds
    abstract fun bindUserDataMapper(
        impl: UserDataMapperImpl
    ): UserDataMapper

    @Binds
    abstract fun bindUserProfileInfoDataMapper(
        impl: UserProfileInfoDataMapperImpl
    ): UserProfileInfoDataMapper

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

    @Binds
    abstract fun bindBookFirestoreMapper(
        impl: BookFirestoreMapperImpl
    ): BookFirestoreMapper

    @Binds
    abstract fun bindSessionFirestoreMapper(
        impl: SessionFirestoreMapperImpl
    ): SessionFirestoreMapper

    @Binds
    abstract fun bindAnnotationFirestoreMapper(
        impl: AnnotationFirestoreMapperImpl
    ): AnnotationFirestoreMapper

    @Binds
    abstract fun bindUserProfileInfoFirestoreMapper(
        impl: UserProfileInfoFirestoreMapperImpl
    ): UserProfileInfoFirestoreMapper

    @Binds
    abstract fun bindUserSearchFirestoreMapper(
        impl: UserSearchFirestoreMapperImpl
    ): UserSearchFirestoreMapper

    @Binds
    @Singleton
    abstract fun bindBookFirestoreDataSource(
        impl: BookFirestoreDataSourceImpl
    ): BookFirestoreDataSource

    @Binds
    @Singleton
    abstract fun bindSessionFirestoreDataSource(
        impl: SessionFirestoreDataSourceImpl
    ): SessionFirestoreDataSource

    @Binds
    @Singleton
    abstract fun bindAnnotationFirestoreDataSource(
        impl: AnnotationFirestoreDataSourceImpl
    ): AnnotationFirestoreDataSource

    @Binds
    @Singleton
    abstract fun bindUserProfileInfoFirestoreDataSource(
        impl: UserProfileInfoFirestoreDataSourceImpl
    ): UserProfileInfoFirestoreDataSource

    @Binds
    @Singleton
    abstract fun bindUserSearchFirestoreDataSource(
        impl: UserSearchFirestoreDataSourceImpl
    ): UserSearchFirestoreDataSource
}
