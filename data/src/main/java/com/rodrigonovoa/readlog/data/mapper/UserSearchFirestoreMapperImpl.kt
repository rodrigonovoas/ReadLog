package com.rodrigonovoa.readlog.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import javax.inject.Inject

class UserSearchFirestoreMapperImpl @Inject constructor() : UserSearchFirestoreMapper {
    override fun toDomain(snapshot: DocumentSnapshot): UserSearchResult? {
        val userId = snapshot.reference.parent.parent?.id ?: return null
        val username = snapshot.getString("username")?.ifBlank { null } ?: return null
        return UserSearchResult(userId = userId, username = username)
    }
}
