package com.tlnacl.githubcompose.feature.github.presentation
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.core.presentation.UiText
import com.tlnacl.githubcompose.core.presentation.toUiText
import com.tlnacl.githubcompose.feature.github.domain.GithubRemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUi(
    val login: String,
    val avatarUrl: String,
    val name: String,
    val location: UiText,
    val blogUrl: String,
    val htmlUrl: String,
    val publicRepos: String,
    val followers: String,
    val following: String,
)
data class RepoUi(
    val id: Long,
    val name: String,
    val description: UiText,
    val language: UiText,
    val watchers: String,
    val stars: String,
    val forks: String,
    val htmlUrl: String,
)

@Stable
data class ReposState(
    val user: ProfileUi? = null,
    val repos: List<RepoUi> = emptyList(),
    val isUserLoading: Boolean = false,
    val isReposLoading: Boolean = false,
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
    private var profileJob: Job? = null
    private var reposJob: Job? = null
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
        profileJob?.cancel()
        reposJob?.cancel()
        profileJob = viewModelScope.launch {
            _state.update { it.copy(isUserLoading = true, userError = null) }
            when (val result = source.getUser(login)) {
                is Result.Success -> {
                    val user = result.data
                    _state.update {
                        it.copy(
                            isUserLoading = false,
                            user = ProfileUi(
                                user.login,
                                user.avatarUrl,
                                user.name.ifBlank { user.login },
                                user.location?.takeIf(
                                    String::isNotBlank,
                                )?.let(UiText::DynamicString)
                                    ?: UiText.StringResource(R.string.location_not_defined),
                                user.blogUrl,
                                user.htmlUrl,
                                user.publicRepos.toString(),
                                user.followers.toString(),
                                user.following.toString(),
                            ),
                        )
                    }
                }

                is Result.Error -> _state.update {
                    it.copy(isUserLoading = false, userError = result.error.toUiText())
                }
            }
        }
        reposJob = viewModelScope.launch {
            _state.update { it.copy(isReposLoading = true, reposError = null) }
            when (val result = source.getRepos(login)) {
                is Result.Success -> _state.update {
                    it.copy(
                        isReposLoading = false,
                        repos = result.data.map { repo ->
                            RepoUi(
                                repo.id,
                                repo.name,
                                repo.description?.let(UiText::DynamicString)
                                    ?: UiText.StringResource(R.string.no_description),
                                repo.language?.uppercase(
                                    Locale.getDefault(),
                                )?.let(UiText::DynamicString)
                                    ?: UiText.StringResource(R.string.language_not_defined),
                                repo.watchersCount.toString(),
                                repo.stargazersCount.toString(),
                                repo.forksCount.toString(),
                                repo.htmlUrl,
                            )
                        },
                    )
                }

                is Result.Error -> _state.update {
                    it.copy(isReposLoading = false, reposError = result.error.toUiText())
                }
            }
        }
    }
}
