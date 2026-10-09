package com.tlnacl.githubcompose.feature.github.presentation.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme
import com.tlnacl.githubcompose.core.designsystem.UserRow
import com.tlnacl.githubcompose.core.presentation.ErrorMessage
import com.tlnacl.githubcompose.core.presentation.ObserveAsEvents
import com.tlnacl.githubcompose.core.presentation.asString
import com.tlnacl.githubcompose.feature.github.presentation.users.UserUi

@Composable
fun FavoritesRoot(
    onNavigateToRepos: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is FavoritesEvent.NavigateToRepos -> onNavigateToRepos(event.login)
            is FavoritesEvent.ShowSnackbar -> snackbar.showSnackbar(event.message.asString(context))
        }
    }
    Scaffold(contentWindowInsets = WindowInsets(0), snackbarHost = {
        SnackbarHost(snackbar)
    }) { padding ->
        FavoritesScreen(state, viewModel::onAction, Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesState,
    onAction: (FavoritesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(stringResource(R.string.favorite))
        }, windowInsets = WindowInsets(0))
        if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { ErrorMessage(it, { onAction(FavoritesAction.Retry) }) }
        LazyColumn(state = rememberLazyListState()) {
            if (!state.isLoading && state.error == null && state.users.isEmpty()) {
                item { Text(stringResource(R.string.no_favorites), Modifier.padding(16.dp)) }
            }
            items(state.users, key = { it.login }) { user ->
                UserRow(
                    user.login,
                    user.avatarUrl,
                    user.htmlUrl,
                    user.isFavorite,
                    user.login.lowercase() in state.pendingFavorites || state.error != null,
                    onClick = { onAction(FavoritesAction.SelectUser(user.login)) },
                    onFavoriteClick = { onAction(FavoritesAction.RemoveFavorite(user)) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    GithubComposeTheme {
        FavoritesScreen(
            FavoritesState(
                users = listOf(UserUi("octocat", "", "https://github.com/octocat", true)),
                isLoading = false,
            ),
            {
            },
        )
    }
}
