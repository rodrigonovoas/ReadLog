package com.rodrigonovoa.readlog.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.rodrigonovoa.readlog.domain.model.UserSearchResult

interface UserSearchFirestoreMapper {
    fun toDomain(snapshot: DocumentSnapshot): UserSearchResult?
}
