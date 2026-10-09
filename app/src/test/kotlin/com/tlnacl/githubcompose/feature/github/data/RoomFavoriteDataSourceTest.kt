package com.tlnacl.githubcompose.feature.github.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

private class FailingFavoriteDao(private val failure: Exception) : FavoriteDao {
    override fun observeFavorites(): Flow<List<FavoriteEntity>> = flow { throw failure }
    override suspend fun upsert(user: FavoriteEntity): Unit = throw failure
    override suspend fun delete(loginKey: String): Unit = throw failure
}

class RoomFavoriteDataSourceTest {
    @Test fun `write and observation failures become typed local errors`() = runTest {
        val source = RoomFavoriteDataSource(FailingFavoriteDao(IOException("disk unavailable")))
        assertThat(source.setFavorite(FavoriteUser("octocat", "", ""), true))
            .isEqualTo(Result.Error(DataError.Local.UNKNOWN))
        assertThat(
            source.observeFavorites().first(),
        ).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
    }

    @Test fun `cancellation propagates from writes and observation`() = runTest {
        val source = RoomFavoriteDataSource(FailingFavoriteDao(CancellationException("cancelled")))
        var writeCancelled = false
        var observationCancelled = false
        try {
            source.setFavorite(FavoriteUser("octocat", "", ""), false)
        } catch (
            _: CancellationException,
        ) {
            writeCancelled = true
        }
        try {
            source.observeFavorites().first()
        } catch (
            _: CancellationException,
        ) {
            observationCancelled = true
        }
        assertThat(writeCancelled).isTrue()
        assertThat(observationCancelled).isTrue()
    }
}
