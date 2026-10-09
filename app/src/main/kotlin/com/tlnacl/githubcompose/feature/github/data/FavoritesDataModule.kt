package com.tlnacl.githubcompose.feature.github.data

import android.content.Context
import androidx.room.Room
import com.tlnacl.githubcompose.feature.github.domain.FavoriteLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesDataModule {
    @Binds
    @Singleton
    abstract fun bindFavoriteLocalDataSource(
        source: RoomFavoriteDataSource,
    ): FavoriteLocalDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object FavoritesDatabaseModule {
    @Provides
    @Singleton
    fun provideFavoritesDatabase(@ApplicationContext context: Context): FavoritesDatabase =
        Room.databaseBuilder(context, FavoritesDatabase::class.java, "favorites.db").build()

    @Provides
    fun provideFavoriteDao(database: FavoritesDatabase): FavoriteDao = database.favoriteDao()
}
