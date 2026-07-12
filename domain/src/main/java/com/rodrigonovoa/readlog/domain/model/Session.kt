package com.rodrigonovoa.readlog.domain.model

data class Session(
    val sessionId: Int = 0,
    val remoteId: String = "",
    val bookId: Int,
    val bookRemoteId: String = "",
    val time: Long,
    val creationDate: Long = System.currentTimeMillis(),
    val lastModified: Long = 0L,
)
