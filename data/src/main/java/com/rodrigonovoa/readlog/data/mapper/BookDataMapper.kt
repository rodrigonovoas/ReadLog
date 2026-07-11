package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.domain.model.Book

interface BookDataMapper {
    fun toDomain(entity: BookEntity): Book
    fun toEntity(domain: Book): BookEntity
}
