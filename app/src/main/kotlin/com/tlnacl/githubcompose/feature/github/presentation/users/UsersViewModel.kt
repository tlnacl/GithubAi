package com.tlnacl.githubcompose.feature.github.presentation.users

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.core.presentation.UiText
import com.tlnacl.githubcompose.core.presentation.toUiText
import com.tlnacl.githubcompose.feature.github.domain.FavoriteLocalDataSource
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser
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
    val favoritesError: UiText? = null,
    val isFavoritesLoading: Boolean = true,
    val pendingFavorites: Set<String> = emptySet(),
)

sealed interface UsersAction {
    data class SearchQueryChanged(val query: String) : UsersAction
    data object RetryFavorites : UsersAction
    data class ToggleFavorite(val user: UserUi) : UsersAction
    data object Refresh : UsersAction
    data class SelectUser(val login: String) : UsersAction
}

sealed interface UsersEvent {
    data class NavigateToRepos(val login: String) : UsersEvent
    data class ShowSnackbar(val message: UiText) : UsersEvent
}

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val source: GithubRemoteDataSource,
    private val favorites: FavoriteLocalDataSource,
) : ViewModel() {
    private val _state = MutableStateFlow(UsersState())
    val state = _state.asStateFlow()
    private val _events = Channel<UsersEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private data class LoadRequest(val query: String, val debounce: Boolean = false)
    private val requests = Channel<LoadRequest>(Channel.CONFLATED)

    private val favoriteRetries = Channel<Unit>(Channel.CONFLATED)
    private var favoriteLogins: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            favoriteRetries.receiveAsFlow().collectLatest {
                _state.update { it.copy(favoritesError = null) }
                favorites.observeFavorites().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            favoriteLogins = result.data.map { it.login.lowercase() }.toSet()
                            _state.update { state ->
                                state.copy(
                                    users = state.users.map { user ->
                                        val saved = user.login.lowercase() in favoriteLogins
                                        user.copy(isFavorite = saved)
                                    },
                                    favoritesError = null,
                                    isFavoritesLoading = false,
                                )
                            }
                        }

                        is Result.Error -> _state.update {
                            it.copy(
                                favoritesError = result.error.toUiText(),
                                isFavoritesLoading = false,
                            )
                        }
                    }
                }
            }
        }
        favoriteRetries.trySend(Unit)
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
            UsersAction.RetryFavorites -> favoriteRetries.trySend(Unit)

            is UsersAction.ToggleFavorite -> toggleFavorite(action.user)

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

    private fun toggleFavorite(user: UserUi) {
        val key = user.login.lowercase()
        if (key in _state.value.pendingFavorites ||
            (_state.value.favoritesError != null || _state.value.isFavoritesLoading)
        ) {
            return
        }
        val save = key !in favoriteLogins
        _state.update { it.copy(pendingFavorites = it.pendingFavorites + key) }
        viewModelScope.launch {
            try {
                val result = favorites.setFavorite(
                    FavoriteUser(user.login, user.avatarUrl, user.htmlUrl),
                    save,
                )
                if (result is Result.Error) {
                    _events.send(
                        UsersEvent.ShowSnackbar(result.error.toUiText()),
                    )
                }
            } finally {
                _state.update { it.copy(pendingFavorites = it.pendingFavorites - key) }
            }
        }
    }

    private suspend fun loadUsers(query: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = if (query.isEmpty()) source.getUsers() else source.searchUsers(query)
        when (result) {
            is Result.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        users = result.data.map { user ->
                            user.toUserUi().copy(
                                isFavorite =
                                    user.login.lowercase() in favoriteLogins,
                            )
                        },
                    )
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
