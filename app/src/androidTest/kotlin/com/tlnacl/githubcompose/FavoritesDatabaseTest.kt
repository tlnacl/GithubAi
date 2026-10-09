package com.tlnacl.githubcompose

import android.database.sqlite.SQLiteFullException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.feature.github.data.FavoriteDao
import com.tlnacl.githubcompose.feature.github.data.FavoriteEntity
import com.tlnacl.githubcompose.feature.github.data.FavoritesDatabase
import com.tlnacl.githubcompose.feature.github.data.RoomFavoriteDataSource
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesDatabaseTest {
    @Test fun persistenceUniquenessOrderingAndRemoval() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "favorites-test.db"
        context.deleteDatabase(name)
        try {
            var db = Room.databaseBuilder(context, FavoritesDatabase::class.java, name).build()
            var source = RoomFavoriteDataSource(db.favoriteDao())
            source.setFavorite(FavoriteUser("Zebra", "avatar1", "profile1"), true)
            source.setFavorite(FavoriteUser("android", "avatar2", "profile2"), true)
            source.setFavorite(FavoriteUser("ZEBRA", "updated-avatar", "updated-profile"), true)
            val initial = (source.observeFavorites().first() as Result.Success).data
            assertEquals(listOf("android", "ZEBRA"), initial.map { it.login })
            db.close()
            db = Room.databaseBuilder(context, FavoritesDatabase::class.java, name).build()
            try {
                source = RoomFavoriteDataSource(db.favoriteDao())
                val reopened = (source.observeFavorites().first() as Result.Success).data
                assertEquals(initial, reopened)
                source.setFavorite(FavoriteUser("zEbRa", "", ""), false)
                assertEquals(
                    listOf("android"),
                    (source.observeFavorites().first() as Result.Success).data.map {
                        it.login
                    },
                )
                source.setFavorite(FavoriteUser("android", "", ""), false)
                assertTrue((source.observeFavorites().first() as Result.Success).data.isEmpty())
            } finally {
                db.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test fun diskFullErrorsAreTyped() = runBlocking {
        val dao = object : FavoriteDao {
            override fun observeFavorites(): Flow<List<FavoriteEntity>> = flow {
                throw SQLiteFullException()
            }
            override suspend fun upsert(user: FavoriteEntity): Unit = throw SQLiteFullException()
            override suspend fun delete(loginKey: String): Unit = throw SQLiteFullException()
        }
        val source = RoomFavoriteDataSource(dao)
        assertEquals(Result.Error(DataError.Local.DISK_FULL), source.observeFavorites().first())
        assertEquals(
            Result.Error(DataError.Local.DISK_FULL),
            source.setFavorite(FavoriteUser("cat", "", ""), true),
        )
    }
}
