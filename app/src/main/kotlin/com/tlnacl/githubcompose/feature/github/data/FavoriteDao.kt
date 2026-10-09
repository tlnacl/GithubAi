package com.tlnacl.githubcompose.feature.github.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY loginKey ASC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Upsert
    suspend fun upsert(user: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE loginKey = :loginKey")
    suspend fun delete(loginKey: String)
}
