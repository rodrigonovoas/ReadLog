package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.ReadLogDatabase
import com.rodrigonovoa.readlog.domain.repository.LocalDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataRepositoryImpl @Inject constructor(
    private val database: ReadLogDatabase,
) : LocalDataRepository {
    override suspend fun clearAllLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
