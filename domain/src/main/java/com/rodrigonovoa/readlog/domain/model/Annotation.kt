package com.rodrigonovoa.readlog.domain.model

data class Annotation(
    val annotationId: Int = 0,
    val remoteId: String = "",
    val sessionId: Int,
    val sessionRemoteId: String = "",
    val annotation: String,
    val creationDate: Long = System.currentTimeMillis(),
    val lastModified: Long = 0L,
)
