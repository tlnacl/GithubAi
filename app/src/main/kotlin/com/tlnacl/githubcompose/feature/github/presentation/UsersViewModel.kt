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
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserUi(val login: String, val avatarUrl: String, val htmlUrl: String)

@Stable
data class UsersState(
    val users: List<UserUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
)
sealed interface UsersAction {
    data object Refresh : UsersAction
    data class SelectUser(val login: String) : UsersAction
}
sealed interface UsersEvent {
    data class NavigateToRepos(val login: String) : UsersEvent
    data class ShowSnackbar(val message: UiText) : UsersEvent
}

@HiltViewModel
class UsersViewModel @Inject constructor(private val source: GithubRemoteDataSource) : ViewModel() {
    private val _state = MutableStateFlow(UsersState())
    val state = _state.asStateFlow()
    private val _events = Channel<UsersEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var loadJob: Job? = null
    init {
        loadUsers()
    }
    fun onAction(action: UsersAction) {
        when (action) {
            UsersAction.Refresh -> loadUsers()

            is UsersAction.SelectUser -> viewModelScope.launch {
                _events.send(UsersEvent.NavigateToRepos(action.login))
            }
        }
    }
    private fun loadUsers() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = source.getUsers()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            users = result.data.map { user ->
                                UserUi(user.login, user.avatarUrl, user.htmlUrl)
                            },
                        )
                    }
                    _events.send(
                        UsersEvent.ShowSnackbar(UiText.StringResource(R.string.users_loaded)),
                    )
                }

                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.toUiText())
                }
            }
        }
    }
}
