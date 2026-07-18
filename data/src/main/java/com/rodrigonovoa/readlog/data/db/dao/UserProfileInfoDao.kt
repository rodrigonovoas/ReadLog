package com.rodrigonovoa.readlog.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rodrigonovoa.readlog.data.db.entity.UserProfileInfoEntity

@Dao
interface UserProfileInfoDao {

    @Query("SELECT * FROM user_profile_info WHERE userId = :userId")
    suspend fun getByUserId(userId: String): UserProfileInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(info: UserProfileInfoEntity)
}
