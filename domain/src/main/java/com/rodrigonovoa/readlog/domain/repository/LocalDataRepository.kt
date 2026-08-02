package com.rodrigonovoa.readlog.domain.repository

interface LocalDataRepository {
    suspend fun clearAllLocalData()
}
