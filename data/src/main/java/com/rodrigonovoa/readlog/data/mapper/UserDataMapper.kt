package com.rodrigonovoa.readlog.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.rodrigonovoa.readlog.domain.model.User

interface UserDataMapper {
    fun toDomain(firebaseUser: FirebaseUser): User
}
