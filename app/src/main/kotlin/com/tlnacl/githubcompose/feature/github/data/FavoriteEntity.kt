package com.tlnacl.githubcompose.feature.github.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val loginKey: String,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
)

fun FavoriteUser.toEntity() = FavoriteEntity(login.lowercase(), login, avatarUrl, htmlUrl)
fun FavoriteEntity.toFavoriteUser() = FavoriteUser(login, avatarUrl, htmlUrl)
