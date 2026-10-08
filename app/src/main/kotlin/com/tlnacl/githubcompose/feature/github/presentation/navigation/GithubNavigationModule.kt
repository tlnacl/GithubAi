package com.tlnacl.githubcompose.feature.github.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

// Decode framework navigation arguments at the graph boundary. ViewModel logic
// receives the typed route and can be tested without an Android Bundle runtime.
@Module
@InstallIn(ViewModelComponent::class)
object GithubNavigationModule {
    @Provides
    fun provideReposRoute(savedStateHandle: SavedStateHandle): ReposRoute =
        savedStateHandle.toRoute()
}
