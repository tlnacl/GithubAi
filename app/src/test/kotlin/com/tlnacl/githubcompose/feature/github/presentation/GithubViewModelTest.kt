package com.tlnacl.githubcompose.feature.github.presentation
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
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
import com.tlnacl.githubcompose.feature.github.domain.GithubRemoteDataSource
import com.tlnacl.githubcompose.feature.github.domain.Repo
import com.tlnacl.githubcompose.feature.github.domain.User
import com.tlnacl.githubcompose.feature.github.presentation.navigation.ReposRoute
import com.tlnacl.githubcompose.feature.github.presentation.repos.ReposAction
import com.tlnacl.githubcompose.feature.github.presentation.repos.ReposEvent
import com.tlnacl.githubcompose.feature.github.presentation.repos.ReposViewModel
import com.tlnacl.githubcompose.feature.github.presentation.users.UsersAction
import com.tlnacl.githubcompose.feature.github.presentation.users.UsersEvent
import com.tlnacl.githubcompose.feature.github.presentation.users.UsersViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class FakeGithubSource : GithubRemoteDataSource {
    var users: Result<List<User>, DataError.Network> = Result.Success(
        listOf(User("octocat", "avatar", "https://github.com/octocat")),
    )
    var profile: Result<User, DataError.Network> = Result.Success(
        User("octocat", "avatar", "https://github.com/octocat", blogUrl = "example.com"),
    )
    var repos: Result<List<Repo>, DataError.Network> = Result.Success(
        listOf(Repo(1, "Hello", null, 12, 4, 30, "Kotlin", "https://github.com/octocat/Hello")),
    )
    val searchQueries = mutableListOf<String>()
    var searchGate: CompletableDeferred<Unit>? = null
    var cancelledSearches = 0
    override suspend fun searchUsers(query: String): Result<List<User>, DataError.Network> {
        searchQueries.add(query)
        try {
            searchGate?.await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            cancelledSearches++
            throw e
        }
        return users
    }
    var requestedLogin: String? = null
    var usersGate: CompletableDeferred<Unit>? = null
    var profileGate: CompletableDeferred<Unit>? = null
    var reposGate: CompletableDeferred<Unit>? = null
    var usersCalls = 0
    var profileCalls = 0
    var reposCalls = 0
    override suspend fun getUsers(): Result<List<User>, DataError.Network> {
        usersCalls++
        usersGate?.await()
        return users
    }
    override suspend fun getUser(login: String): Result<User, DataError.Network> {
        profileCalls++
        profileGate?.await()
        requestedLogin =
            login
        return profile
    }
    override suspend fun getRepos(login: String): Result<List<Repo>, DataError.Network> {
        reposCalls++
        reposGate?.await()
        return repos
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GithubViewModelTest {
    @BeforeEach fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test fun `users map into loaded UI state`() = runTest {
        val vm = UsersViewModel(FakeGithubSource(), FakeFavorites())
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(vm.state.value.users.single().login).isEqualTo("octocat")
    }

    @Test fun `retry clears users error and loads results`() = runTest {
        val source = FakeGithubSource()
        source.users = Result.Error(DataError.Network.NO_INTERNET)
        val vm = UsersViewModel(source, FakeFavorites())
        assertThat(vm.state.value.error).isNotNull()
        source.users = Result.Success(listOf(User("android", "", "")))
        vm.onAction(UsersAction.Refresh)
        assertThat(vm.state.value.error).isNull()
        assertThat(vm.state.value.users.single().login).isEqualTo("android")
    }

    @Test fun `select user emits navigation event`() = runTest {
        val vm = UsersViewModel(FakeGithubSource(), FakeFavorites())
        vm.events.test {
            assertThat(awaitItem()).isInstanceOf(UsersEvent.ShowSnackbar::class)
            vm.onAction(UsersAction.SelectUser("octocat"))
            assertThat(awaitItem()).isEqualTo(UsersEvent.NavigateToRepos("octocat"))
        }
    }

    @Test fun `search waits one second after latest keystroke`() = runTest {
        val source = FakeGithubSource()
        val vm = UsersViewModel(source, FakeFavorites())
        vm.onAction(UsersAction.SearchQueryChanged("oct"))
        advanceTimeBy(999)
        runCurrent()
        assertThat(source.searchQueries).hasSize(0)
        vm.onAction(UsersAction.SearchQueryChanged("octocat"))
        advanceTimeBy(999)
        runCurrent()
        assertThat(source.searchQueries).hasSize(0)
        advanceTimeBy(1)
        runCurrent()
        assertThat(source.searchQueries).isEqualTo(listOf("octocat"))
        assertThat(vm.state.value.users.single().login).isEqualTo("octocat")
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test fun `loading waits for debounce while existing results remain`() = runTest {
        val source = FakeGithubSource()
        val vm = UsersViewModel(source, FakeFavorites())
        source.searchGate = CompletableDeferred()
        vm.onAction(UsersAction.SearchQueryChanged("android"))
        runCurrent()
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(vm.state.value.users.single().login).isEqualTo("octocat")
        advanceTimeBy(999)
        runCurrent()
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(source.searchQueries).hasSize(0)
        advanceTimeBy(1)
        runCurrent()
        assertThat(vm.state.value.isLoading).isTrue()
        assertThat(source.searchQueries).isEqualTo(listOf("android"))
        source.searchGate?.complete(Unit)
        runCurrent()
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test fun `typing cancels an in flight search before the next debounce`() = runTest {
        val source = FakeGithubSource()
        val vm = UsersViewModel(source, FakeFavorites())
        source.searchGate = CompletableDeferred()
        vm.onAction(UsersAction.SearchQueryChanged("old"))
        advanceTimeBy(1_000)
        runCurrent()
        vm.onAction(UsersAction.SearchQueryChanged("new"))
        runCurrent()
        assertThat(source.cancelledSearches).isEqualTo(1)
        assertThat(vm.state.value.isLoading).isFalse()
        source.searchGate = null
        source.users = Result.Success(listOf(User("new", "", "")))
        advanceTimeBy(1_000)
        runCurrent()
        assertThat(vm.state.value.users.single().login).isEqualTo("new")
    }

    @Test fun `clearing search restores default users and cancels pending search`() = runTest {
        val source = FakeGithubSource()
        val vm = UsersViewModel(source, FakeFavorites())
        vm.onAction(UsersAction.SearchQueryChanged("oct"))
        advanceTimeBy(500)
        vm.onAction(UsersAction.SearchQueryChanged(""))
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()
        assertThat(source.searchQueries).hasSize(0)
        assertThat(source.usersCalls).isEqualTo(2)
        assertThat(vm.state.value.users.single().login).isEqualTo("octocat")
    }

    @Test fun `search failure retries current trimmed query immediately`() = runTest {
        val source = FakeGithubSource()
        val vm = UsersViewModel(source, FakeFavorites())
        source.users = Result.Error(DataError.Network.NO_INTERNET)
        vm.onAction(UsersAction.SearchQueryChanged(" android "))
        advanceTimeBy(1_000)
        runCurrent()
        assertThat(vm.state.value.error).isNotNull()
        source.users = Result.Success(emptyList())
        vm.onAction(UsersAction.Refresh)
        runCurrent()
        assertThat(source.searchQueries).isEqualTo(listOf("android", "android"))
        assertThat(vm.state.value.error).isNull()
        assertThat(vm.state.value.users).hasSize(0)
    }

    @Test fun `favorite updates synchronize hearts without another network request`() = runTest {
        val source = FakeGithubSource()
        val favorites = FakeFavorites()
        val vm = UsersViewModel(source, favorites)
        vm.onAction(UsersAction.ToggleFavorite(vm.state.value.users.single()))
        assertThat(vm.state.value.users.single().isFavorite).isTrue()
        assertThat(source.usersCalls).isEqualTo(1)
        favorites.setFavorite(FavoriteUser("octocat", "", ""), false)
        assertThat(vm.state.value.users.single().isFavorite).isFalse()
    }

    @Test fun `pending heart ignores repeated taps and failure preserves saved state`() = runTest {
        val favorites = FakeFavorites()
        favorites.writeGate = CompletableDeferred()
        favorites.writeError = DataError.Local.DISK_FULL
        val vm = UsersViewModel(FakeGithubSource(), favorites)
        val user = vm.state.value.users.single()
        vm.onAction(UsersAction.ToggleFavorite(user))
        vm.onAction(UsersAction.ToggleFavorite(user))
        assertThat(favorites.writes).isEqualTo(1)
        assertThat(vm.state.value.pendingFavorites).isEqualTo(setOf("octocat"))
        vm.events.test {
            awaitItem() // Initial default-list completion.
            favorites.writeGate?.complete(Unit)
            assertThat(awaitItem()).isInstanceOf(UsersEvent.ShowSnackbar::class)
        }
        assertThat(vm.state.value.users.single().isFavorite).isFalse()
        assertThat(vm.state.value.pendingFavorites).hasSize(0)
    }

    @Test fun `search favorites stay current and observation can retry`() = runTest {
        val favorites = FakeFavorites()
        val source = FakeGithubSource()
        val vm = UsersViewModel(source, favorites)
        vm.onAction(UsersAction.SearchQueryChanged("octocat"))
        favorites.setFavorite(FavoriteUser("Octocat", "", ""), true)
        advanceTimeBy(1_000)
        runCurrent()
        assertThat(vm.state.value.users.single().isFavorite).isTrue()
        favorites.results.value = Result.Error(DataError.Local.UNKNOWN)
        assertThat(vm.state.value.favoritesError).isNotNull()
        assertThat(vm.state.value.users).hasSize(1)
        vm.onAction(UsersAction.RetryFavorites)
        assertThat(favorites.observations).isEqualTo(2)
        favorites.results.value = Result.Success(emptyList())
        assertThat(vm.state.value.favoritesError).isNull()
        assertThat(vm.state.value.users.single().isFavorite).isFalse()
    }

    @Test fun `repos use typed route login and format UI counters`() = runTest {
        val source = FakeGithubSource()
        val vm = ReposViewModel(source, ReposRoute("octocat"))
        assertThat(source.requestedLogin).isEqualTo("octocat")
        assertThat(vm.state.value.repos.single().stars).isEqualTo("30")
        assertThat(vm.state.value.user?.name).isEqualTo("octocat")
    }

    @Test fun `profile failure does not prevent repository loading`() = runTest {
        val source = FakeGithubSource()
        source.profile = Result.Error(DataError.Network.NOT_FOUND)
        val vm = ReposViewModel(source, ReposRoute("octocat"))
        assertThat(vm.state.value.userError).isNotNull()
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(vm.state.value.repos).hasSize(1)
    }

    @Test fun `missing blog dialog is state owned by viewmodel`() = runTest {
        val source = FakeGithubSource()
        source.profile = Result.Success(User("octocat", "", ""))
        val vm = ReposViewModel(source, ReposRoute("octocat"))
        vm.onAction(ReposAction.OpenBlog)
        assertThat(vm.state.value.showBlogDialog).isTrue()
        vm.onAction(ReposAction.DismissBlogDialog)
        assertThat(vm.state.value.showBlogDialog).isFalse()
    }

    @Test fun `blog without scheme opens as HTTPS`() = runTest {
        val vm = ReposViewModel(FakeGithubSource(), ReposRoute("octocat"))
        vm.events.test {
            vm.onAction(ReposAction.OpenBlog)
            assertThat(awaitItem()).isEqualTo(ReposEvent.OpenUrl("https://example.com"))
        }
    }

    @Test fun `unsafe link emits user facing message`() = runTest {
        val vm = ReposViewModel(FakeGithubSource(), ReposRoute("octocat"))
        vm.events.test {
            vm.onAction(ReposAction.OpenRepo("javascript://alert"))
            assertThat(awaitItem()).isInstanceOf(ReposEvent.ShowSnackbar::class)
        }
    }

    @Test fun `back action emits back navigation`() = runTest {
        val vm = ReposViewModel(FakeGithubSource(), ReposRoute("octocat"))
        vm.events.test {
            vm.onAction(ReposAction.Back)
            assertThat(awaitItem()).isEqualTo(ReposEvent.Back)
        }
    }

    @Test fun `overlapping user refreshes share request and emit one completion event`() = runTest {
        val source = FakeGithubSource()
        val gate = CompletableDeferred<Unit>()
        source.usersGate = gate
        val vm = UsersViewModel(source, FakeFavorites())
        vm.onAction(UsersAction.Refresh)
        vm.onAction(UsersAction.Refresh)
        assertThat(source.usersCalls).isEqualTo(1)
        assertThat(vm.state.value.isLoading).isTrue()
        vm.events.test {
            gate.complete(Unit)
            assertThat(awaitItem()).isInstanceOf(UsersEvent.ShowSnackbar::class)
            expectNoEvents()
        }
        assertThat(vm.state.value.isLoading).isFalse()
        source.usersGate = null
        vm.onAction(UsersAction.Refresh)
        assertThat(source.usersCalls).isEqualTo(2)
    }

    @Test fun `overlapping repos refresh shares load and waits for both results`() = runTest {
        val source = FakeGithubSource()
        val gate = CompletableDeferred<Unit>()
        source.profileGate = gate
        val vm = ReposViewModel(source, ReposRoute("octocat"))
        assertThat(vm.state.value.isLoading).isTrue()
        assertThat(vm.state.value.repos).hasSize(0)
        vm.onAction(ReposAction.Refresh)
        vm.onAction(ReposAction.Refresh)
        assertThat(source.profileCalls).isEqualTo(1)
        assertThat(source.reposCalls).isEqualTo(1)
        gate.complete(Unit)
        assertThat(vm.state.value.isLoading).isFalse()
        source.profileGate = null
        vm.onAction(ReposAction.Refresh)
        assertThat(source.profileCalls).isEqualTo(2)
        assertThat(source.reposCalls).isEqualTo(2)
    }

    @Test fun `profile remains hidden until slower repositories complete`() = runTest {
        val source = FakeGithubSource()
        val gate = CompletableDeferred<Unit>()
        source.reposGate = gate
        val vm = ReposViewModel(source, ReposRoute("octocat"))
        assertThat(source.profileCalls).isEqualTo(1)
        assertThat(source.reposCalls).isEqualTo(1)
        assertThat(vm.state.value.user).isNull()
        assertThat(vm.state.value.isLoading).isTrue()
        gate.complete(Unit)
        assertThat(vm.state.value.user?.login).isEqualTo("octocat")
        assertThat(vm.state.value.repos).hasSize(1)
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test fun `profile error waits for repositories before publishing both outcomes`() = runTest {
        val source = FakeGithubSource()
        source.profile = Result.Error(DataError.Network.NOT_FOUND)
        val gate = CompletableDeferred<Unit>()
        source.reposGate = gate
        val vm = ReposViewModel(source, ReposRoute("octocat"))
        assertThat(vm.state.value.userError).isNull()
        assertThat(vm.state.value.repos).hasSize(0)
        gate.complete(Unit)
        assertThat(vm.state.value.userError).isNotNull()
        assertThat(vm.state.value.repos).hasSize(1)
        assertThat(vm.state.value.isLoading).isFalse()
    }
}
