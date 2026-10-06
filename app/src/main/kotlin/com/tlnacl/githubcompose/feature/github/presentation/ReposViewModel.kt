package com.tlnacl.githubcompose.feature.github.presentation
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.core.presentation.UiText
import com.tlnacl.githubcompose.core.presentation.toUiText
import com.tlnacl.githubcompose.core.util.ControlledRunner
import com.tlnacl.githubcompose.feature.github.domain.GithubRemoteDataSource
import com.tlnacl.githubcompose.feature.github.domain.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
data class ReposState(
    val user: ProfileUi? = null,
    val repos: List<RepoUi> = emptyList(),
    val isLoading: Boolean = false,
    val userError: UiText? = null,
    val reposError: UiText? = null,
    val showBlogDialog: Boolean = false,
)
sealed interface ReposAction {
    data object Back : ReposAction
    data object Refresh : ReposAction
    data object OpenProfile : ReposAction
    data object OpenBlog : ReposAction
    data object DismissBlogDialog : ReposAction
    data object LinkUnavailable : ReposAction
    data class OpenRepo(val url: String) : ReposAction
}
sealed interface ReposEvent {
    data object Back : ReposEvent
    data class OpenUrl(val url: String) : ReposEvent
    data class ShowSnackbar(val message: UiText) : ReposEvent
}

@HiltViewModel
class ReposViewModel @Inject constructor(
    private val source: GithubRemoteDataSource,
    route: ReposRoute,
) : ViewModel() {
    private val login = route.login
    private val _state = MutableStateFlow(ReposState())
    val state = _state.asStateFlow()
    private val _events = Channel<ReposEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val loadRunner = ControlledRunner<Unit>()
    init {
        load()
    }
    fun onAction(action: ReposAction) {
        when (action) {
            ReposAction.Back -> emit(ReposEvent.Back)

            ReposAction.Refresh -> load()

            ReposAction.OpenProfile -> openUrl(state.value.user?.htmlUrl.orEmpty())

            ReposAction.OpenBlog -> {
                val url = state.value.user?.blogUrl.orEmpty()
                if (url.isBlank()) {
                    _state.update {
                        it.copy(showBlogDialog = true)
                    }
                } else {
                    openUrl(url)
                }
            }

            ReposAction.DismissBlogDialog -> _state.update { it.copy(showBlogDialog = false) }

            ReposAction.LinkUnavailable -> emit(
                ReposEvent.ShowSnackbar(UiText.StringResource(R.string.link_unavailable)),
            )

            is ReposAction.OpenRepo -> openUrl(action.url)
        }
    }
    private fun emit(event: ReposEvent) {
        viewModelScope.launch { _events.send(event) }
    }
    private fun openUrl(value: String) {
        val url = if (value.contains("://")) value else "https://$value"
        val valid = runCatching { URI(url) }.getOrNull()?.let { uri ->
            uri.scheme in listOf("https", "http") && !uri.host.isNullOrBlank()
        } == true
        if (valid) emit(ReposEvent.OpenUrl(url)) else onAction(ReposAction.LinkUnavailable)
    }
    private fun load() {
        viewModelScope.launch {
            loadRunner.joinPreviousOrRun {
                _state.update {
                    it.copy(
                        isLoading = true,
                        userError = null,
                        reposError = null,
                    )
                }
                val (userResult, reposResult) = coroutineScope {
                    val user = async { source.getUser(login) }
                    val repos = async { source.getRepos(login) }
                    user.await() to repos.await()
                }
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        user = when (userResult) {
                            is Result.Success -> userResult.data.toProfileUi()
                            is Result.Error -> current.user
                        },
                        repos = when (reposResult) {
                            is Result.Success -> reposResult.data.map(Repo::toRepoUi)
                            is Result.Error -> current.repos
                        },
                        userError = when (userResult) {
                            is Result.Success -> null
                            is Result.Error -> userResult.error.toUiText()
                        },
                        reposError = when (reposResult) {
                            is Result.Success -> null
                            is Result.Error -> reposResult.error.toUiText()
                        },
                    )
                }
            }
        }
    }
}
