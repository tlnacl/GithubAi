package com.tlnacl.githubcompose.feature.github.domain

import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.EmptyResult
import com.tlnacl.githubcompose.core.domain.Result
import kotlinx.coroutines.flow.Flow

data class FavoriteUser(val login: String, val avatarUrl: String, val htmlUrl: String)

interface FavoriteLocalDataSource {
    fun observeFavorites(): Flow<Result<List<FavoriteUser>, DataError.Local>>
    suspend fun setFavorite(user: FavoriteUser, favorite: Boolean): EmptyResult<DataError.Local>
}
