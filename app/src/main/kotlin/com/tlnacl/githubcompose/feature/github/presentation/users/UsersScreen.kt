package com.tlnacl.githubcompose.feature.github.presentation.users
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

@Composable
fun UsersRoot(onNavigateToRepos: (String) -> Unit, viewModel: UsersViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is UsersEvent.NavigateToRepos -> onNavigateToRepos(event.login)
            is UsersEvent.ShowSnackbar -> snackbar.showSnackbar(event.message.asString(context))
        }
    }
    Scaffold(contentWindowInsets = WindowInsets(0), snackbarHost = {
        SnackbarHost(snackbar)
    }) { padding ->
        UsersScreen(state, viewModel::onAction, Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(state: UsersState, onAction: (UsersAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(stringResource(R.string.users_title))
            },
            windowInsets = WindowInsets(0),
            actions = {
                TextButton(onClick = {
                    onAction(UsersAction.Refresh)
                }, enabled = !state.isLoading) {
                    Text(stringResource(R.string.refresh))
                }
            },
        )
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(UsersAction.SearchQueryChanged(it)) },
            label = { Text(stringResource(R.string.search_users)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { ErrorMessage(it, { onAction(UsersAction.Refresh) }) }
        state.favoritesError?.let { ErrorMessage(it, { onAction(UsersAction.RetryFavorites) }) }
        LazyColumn(state = rememberLazyListState()) {
            if (!state.isLoading && state.error == null && state.users.isEmpty()) {
                item { Text(stringResource(R.string.no_users), Modifier.padding(16.dp)) }
            }
            items(state.users, key = { it.login }) { user ->
                UserRow(
                    login = user.login,
                    avatarUrl = user.avatarUrl,
                    profileUrl = user.htmlUrl,
                    isFavorite = user.isFavorite,
                    isPending =
                        user.login.lowercase() in state.pendingFavorites ||
                            state.favoritesError != null,
                    onClick = { onAction(UsersAction.SelectUser(user.login)) },
                    onFavoriteClick = { onAction(UsersAction.ToggleFavorite(user)) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UsersScreenPreview() {
    GithubComposeTheme {
        UsersScreen(
            UsersState(
                users = listOf(
                    UserUi("octocat", "", "https://github.com/octocat"),
                    UserUi("android", "", "https://github.com/android"),
                ),
            ),
            {},
        )
    }
}
