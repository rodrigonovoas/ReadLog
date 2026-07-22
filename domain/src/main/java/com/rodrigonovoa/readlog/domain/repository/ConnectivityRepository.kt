package com.rodrigonovoa.readlog.domain.repository

interface ConnectivityRepository {
    fun isOnline(): Boolean
}
