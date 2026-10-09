package com.tlnacl.githubcompose.feature.github.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.feature.github.FakeFavorites
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser
import com.tlnacl.githubcompose.feature.github.presentation.favorites.FavoritesAction
import com.tlnacl.githubcompose.feature.github.presentation.favorites.FavoritesEvent
import com.tlnacl.githubcompose.feature.github.presentation.favorites.FavoritesViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @BeforeEach fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test fun `favorites observe sorted users and removal reaches empty state`() = runTest {
        val source = FakeFavorites()
        source.setFavorite(FavoriteUser("zebra", "", ""), true)
        source.setFavorite(FavoriteUser("android", "", ""), true)
        val vm = FavoritesViewModel(source)
        assertThat(vm.state.value.users.map { it.login }).isEqualTo(listOf("android", "zebra"))
        assertThat(vm.state.value.isLoading).isFalse()
        for (user in vm.state.value.users) {
            vm.onAction(FavoritesAction.RemoveFavorite(user))
        }
        assertThat(vm.state.value.users).isEmpty()
    }

    @Test fun `failed removal keeps user and ignores repeated taps`() = runTest {
        val source = FakeFavorites()
        source.setFavorite(FavoriteUser("octocat", "", ""), true)
        source.writeError = DataError.Local.UNKNOWN
        source.writeGate = CompletableDeferred()
        val vm = FavoritesViewModel(source)
        val user = vm.state.value.users.single()
        vm.onAction(FavoritesAction.RemoveFavorite(user))
        vm.onAction(FavoritesAction.RemoveFavorite(user))
        assertThat(source.writes).isEqualTo(2) // One initial save and one removal.
        assertThat(vm.state.value.pendingFavorites).isEqualTo(setOf("octocat"))
        vm.events.test {
            source.writeGate?.complete(Unit)
            assertThat(awaitItem()).isInstanceOf(FavoritesEvent.ShowSnackbar::class)
            expectNoEvents()
        }
        assertThat(vm.state.value.users.single().isFavorite).isTrue()
        assertThat(vm.state.value.pendingFavorites).isEmpty()
    }

    @Test fun `observation errors retain data and can be retried`() = runTest {
        val source = FakeFavorites()
        source.setFavorite(FavoriteUser("octocat", "", ""), true)
        val vm = FavoritesViewModel(source)
        source.results.value = Result.Error(DataError.Local.UNKNOWN)
        assertThat(vm.state.value.error).isNotNull()
        assertThat(vm.state.value.users).hasSize(1)
        vm.onAction(FavoritesAction.Retry)
        assertThat(source.observations).isEqualTo(2)
        source.results.value = Result.Success(emptyList())
        assertThat(vm.state.value.error).isNull()
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test fun `selecting favorite emits repository navigation`() = runTest {
        val vm = FavoritesViewModel(FakeFavorites())
        vm.events.test {
            vm.onAction(FavoritesAction.SelectUser("octocat"))
            assertThat(awaitItem()).isEqualTo(FavoritesEvent.NavigateToRepos("octocat"))
        }
    }
}
