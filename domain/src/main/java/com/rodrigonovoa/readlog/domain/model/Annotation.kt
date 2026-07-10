package com.rodrigonovoa.readlog.domain.model

data class Annotation(
    val annotationId: Int = 0,
    val sessionId: Int,
    val annotation: String,
    val creationDate: Long = System.currentTimeMillis()
)
