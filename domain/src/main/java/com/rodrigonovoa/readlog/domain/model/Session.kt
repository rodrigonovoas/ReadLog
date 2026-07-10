package com.rodrigonovoa.readlog.domain.model

data class Session(
    val sessionId: Int = 0,
    val bookId: Int,
    val time: Long,
    val creationDate: Long = System.currentTimeMillis()
)
