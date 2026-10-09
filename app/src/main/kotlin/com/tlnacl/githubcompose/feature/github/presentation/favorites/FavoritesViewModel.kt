package com.tlnacl.githubcompose.feature.github.presentation.favorites

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.core.presentation.UiText
import com.tlnacl.githubcompose.core.presentation.toUiText
import com.tlnacl.githubcompose.feature.github.domain.FavoriteLocalDataSource
import com.tlnacl.githubcompose.feature.github.domain.FavoriteUser
import com.tlnacl.githubcompose.feature.github.presentation.users.UserUi
import com.tlnacl.githubcompose.feature.github.presentation.users.toUserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
data class FavoritesState(
    val users: List<UserUi> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val pendingFavorites: Set<String> = emptySet(),
)

sealed interface FavoritesAction {
    data object Retry : FavoritesAction
    data class RemoveFavorite(val user: UserUi) : FavoritesAction
    data class SelectUser(val login: String) : FavoritesAction
}

sealed interface FavoritesEvent {
    data class NavigateToRepos(val login: String) : FavoritesEvent
    data class ShowSnackbar(val message: UiText) : FavoritesEvent
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val source: FavoriteLocalDataSource) :
    ViewModel() {
    private val _state = MutableStateFlow(FavoritesState())
    val state = _state.asStateFlow()
    private val _events = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val retries = Channel<Unit>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            retries.receiveAsFlow().collectLatest {
                _state.update { it.copy(isLoading = true, error = null) }
                source.observeFavorites().collect { result ->
                    when (result) {
                        is Result.Success -> _state.update {
                            it.copy(
                                users = result.data.map { user ->
                                    user.toUserUi()
                                },
                                isLoading = false,
                                error = null,
                            )
                        }

                        is Result.Error -> _state.update {
                            it.copy(isLoading = false, error = result.error.toUiText())
                        }
                    }
                }
            }
        }
        retries.trySend(Unit)
    }

    fun onAction(action: FavoritesAction) {
        when (action) {
            FavoritesAction.Retry -> retries.trySend(Unit)

            is FavoritesAction.SelectUser -> viewModelScope.launch {
                _events.send(FavoritesEvent.NavigateToRepos(action.login))
            }

            is FavoritesAction.RemoveFavorite -> removeFavorite(action.user)
        }
    }

    private fun removeFavorite(user: UserUi) {
        val key = user.login.lowercase()
        if (key in _state.value.pendingFavorites || _state.value.error != null) return
        _state.update { it.copy(pendingFavorites = it.pendingFavorites + key) }
        viewModelScope.launch {
            try {
                val result = source.setFavorite(
                    FavoriteUser(user.login, user.avatarUrl, user.htmlUrl),
                    false,
                )
                if (result is Result.Error) {
                    _events.send(
                        FavoritesEvent.ShowSnackbar(result.error.toUiText()),
                    )
                }
            } finally {
                _state.update { it.copy(pendingFavorites = it.pendingFavorites - key) }
            }
        }
    }
}
