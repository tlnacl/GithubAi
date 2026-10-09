package com.tlnacl.githubcompose.feature.github.data

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.EmptyResult
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.feature.github.domain.FavoriteLocalDataSource
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias FavoritesResult = Result<List<FavoriteUser>, DataError.Local>

class RoomFavoriteDataSource @Inject constructor(private val dao: FavoriteDao) :
    FavoriteLocalDataSource {
    private val writes = Mutex()

    override fun observeFavorites(): Flow<Result<List<FavoriteUser>, DataError.Local>> =
        dao.observeFavorites().map<List<FavoriteEntity>, FavoritesResult> {
            Result.Success(it.map(FavoriteEntity::toFavoriteUser))
        }.catch { error ->
            if (error is CancellationException) throw error
            if (error !is SQLiteException && error !is java.io.IOException) throw error
            emit(Result.Error(error.toLocalError()))
        }

    override suspend fun setFavorite(
        user: FavoriteUser,
        favorite: Boolean,
    ): EmptyResult<DataError.Local> = writes.withLock {
        try {
            if (favorite) dao.upsert(user.toEntity()) else dao.delete(user.login.lowercase())
            Result.Success(Unit)
        } catch (error: SQLiteException) {
            Result.Error(error.toLocalError())
        } catch (error: java.io.IOException) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}

private fun Throwable.toLocalError() =
    if (this is SQLiteFullException) DataError.Local.DISK_FULL else DataError.Local.UNKNOWN
