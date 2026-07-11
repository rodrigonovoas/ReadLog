package com.rodrigonovoa.readlog.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.rodrigonovoa.readlog.domain.model.User
import javax.inject.Inject

class UserDataMapperImpl @Inject constructor() : UserDataMapper {
    override fun toDomain(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
        )
    }
}
