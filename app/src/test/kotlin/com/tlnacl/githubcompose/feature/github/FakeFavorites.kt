package com.tlnacl.githubcompose.feature.github

import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.EmptyResult
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.feature.github.domain.FavoriteLocalDataSource
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFavorites : FavoriteLocalDataSource {
    val results =
        MutableStateFlow<Result<List<FavoriteUser>, DataError.Local>>(Result.Success(emptyList()))
    var writeError: DataError.Local? = null
    var writeGate: CompletableDeferred<Unit>? = null
    var writes = 0
    var observations = 0
    override fun observeFavorites() = results.also { observations++ }
    override suspend fun setFavorite(
        user: FavoriteUser,
        favorite: Boolean,
    ): EmptyResult<DataError.Local> {
        writes++
        writeGate?.await()
        writeError?.let { return Result.Error(it) }
        val users = (results.value as Result.Success).data.filterNot {
            it.login.equals(user.login, true)
        }
        results.value =
            Result.Success(
                (if (favorite) users + user else users).sortedBy {
                    it.login.lowercase()
                },
            )
        return Result.Success(Unit)
    }
}
