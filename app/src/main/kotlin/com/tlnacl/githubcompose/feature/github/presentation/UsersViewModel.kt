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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
data class UsersState(
    val searchQuery: String = "",
    val users: List<UserUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
)

sealed interface UsersAction {
    data class SearchQueryChanged(val query: String) : UsersAction
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

    private data class LoadRequest(val query: String, val debounce: Boolean = false)
    private val requests = Channel<LoadRequest>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            requests.receiveAsFlow().collectLatest { request ->
                if (request.debounce) delay(1_000.milliseconds)
                loadUsers(request.query)
            }
        }
        requests.trySend(LoadRequest(""))
    }

    fun onAction(action: UsersAction) {
        when (action) {
            UsersAction.Refresh -> if (!_state.value.isLoading) {
                _state.update { it.copy(isLoading = true, error = null) }
                requests.trySend(LoadRequest(_state.value.searchQuery.trim()))
            }

            is UsersAction.SearchQueryChanged -> {
                if (action.query == _state.value.searchQuery) return
                _state.update {
                    it.copy(
                        searchQuery = action.query,
                        isLoading = false,
                        error = null,
                    )
                }
                val query = action.query.trim()
                requests.trySend(LoadRequest(query, debounce = query.isNotEmpty()))
            }

            is UsersAction.SelectUser -> viewModelScope.launch {
                _events.send(UsersEvent.NavigateToRepos(action.login))
            }
        }
    }

    private suspend fun loadUsers(query: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = if (query.isEmpty()) source.getUsers() else source.searchUsers(query)
        when (result) {
            is Result.Success -> {
                _state.update {
                    it.copy(isLoading = false, users = result.data.map { user -> user.toUserUi() })
                }
                if (query.isEmpty()) {
                    _events.send(
                        UsersEvent.ShowSnackbar(UiText.StringResource(R.string.users_loaded)),
                    )
                }
            }

            is Result.Error -> _state.update {
                it.copy(isLoading = false, error = result.error.toUiText())
            }
        }
    }
}
