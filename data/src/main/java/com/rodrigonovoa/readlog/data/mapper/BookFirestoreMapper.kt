package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Book

interface BookFirestoreMapper {
    fun toFirestoreMap(book: Book): Map<String, Any>
    fun fromFirestoreMap(map: Map<String, Any?>, remoteId: String): Book
}
