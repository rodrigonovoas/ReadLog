package com.rodrigonovoa.readlog.domain.repository

interface SyncRepository {
    suspend fun syncAll(userId: String): Result<Unit>
}
