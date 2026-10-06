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
import com.tlnacl.githubcompose.feature.github.domain.GithubRemoteDataSource
import com.tlnacl.githubcompose.feature.github.domain.Repo
import com.tlnacl.githubcompose.feature.github.domain.User
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
        val vm = UsersViewModel(FakeGithubSource())
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(vm.state.value.users.single().login).isEqualTo("octocat")
    }

    @Test fun `retry clears users error and loads results`() = runTest {
        val source = FakeGithubSource()
        source.users = Result.Error(DataError.Network.NO_INTERNET)
        val vm = UsersViewModel(source)
        assertThat(vm.state.value.error).isNotNull()
        source.users = Result.Success(listOf(User("android", "", "")))
        vm.onAction(UsersAction.Refresh)
        assertThat(vm.state.value.error).isNull()
        assertThat(vm.state.value.users.single().login).isEqualTo("android")
    }

    @Test fun `select user emits navigation event`() = runTest {
        val vm = UsersViewModel(FakeGithubSource())
        vm.events.test {
            assertThat(awaitItem()).isInstanceOf(UsersEvent.ShowSnackbar::class)
            vm.onAction(UsersAction.SelectUser("octocat"))
            assertThat(awaitItem()).isEqualTo(UsersEvent.NavigateToRepos("octocat"))
        }
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
        val vm = UsersViewModel(source)
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
