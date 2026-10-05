package com.tlnacl.githubcompose.feature.github.presentation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.designsystem.Avatar
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme
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
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
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
        if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { ErrorMessage(it, { onAction(UsersAction.Refresh) }) }
        LazyColumn {
            item { Text(stringResource(R.string.users_header), Modifier.padding(16.dp)) }
            if (!state.isLoading && state.error == null && state.users.isEmpty()) {
                item { Text(stringResource(R.string.no_users), Modifier.padding(16.dp)) }
            }
            items(state.users, key = { it.login }) { user ->
                Row(
                    Modifier.fillMaxWidth().clickable {
                        onAction(UsersAction.SelectUser(user.login))
                    }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(user.login, user.avatarUrl)
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(user.login, style = MaterialTheme.typography.titleLarge)
                        Text(user.htmlUrl, style = MaterialTheme.typography.bodyMedium)
                    }
                }
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
