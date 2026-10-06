package com.tlnacl.githubcompose.feature.github.presentation
import android.content.ActivityNotFoundException
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import com.tlnacl.githubcompose.core.presentation.UiText
import com.tlnacl.githubcompose.core.presentation.asString

@Composable
fun ReposRoot(onBack: () -> Unit, viewModel: ReposViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ReposEvent.Back -> onBack()

            is ReposEvent.ShowSnackbar -> snackbar.showSnackbar(event.message.asString(context))

            is ReposEvent.OpenUrl -> try {
                uriHandler.openUri(event.url)
            } catch (e: ActivityNotFoundException) {
                viewModel.onAction(ReposAction.LinkUnavailable)
            } catch (e: IllegalArgumentException) {
                viewModel.onAction(ReposAction.LinkUnavailable)
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        ReposScreen(state, viewModel::onAction, Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposScreen(state: ReposState, onAction: (ReposAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(stringResource(R.string.repos_title))
            },
            windowInsets = WindowInsets(0),
            navigationIcon = {
                TextButton(onClick = {
                    onAction(ReposAction.Back)
                }) { Text(stringResource(R.string.back)) }
            },
            actions = {
                TextButton(
                    onClick = { onAction(ReposAction.Refresh) },
                    enabled = !state.isLoading,
                ) {
                    Text(stringResource(R.string.refresh))
                }
            },
        )
        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        LazyColumn {
            item {
                state.userError?.let { ErrorMessage(it, { onAction(ReposAction.Refresh) }) }
                state.user?.let { ProfileHeader(it, onAction) }
                state.reposError?.let { ErrorMessage(it, { onAction(ReposAction.Refresh) }) }
                if (!state.isLoading && state.reposError == null && state.repos.isEmpty()) {
                    Text(stringResource(R.string.no_repos), Modifier.padding(16.dp))
                }
            }
            items(state.repos, key = { it.id }) { repo ->
                Column(
                    Modifier.fillMaxWidth().clickable {
                        onAction(ReposAction.OpenRepo(repo.htmlUrl))
                    }.padding(16.dp),
                ) {
                    Text(repo.name, style = MaterialTheme.typography.titleLarge)
                    Text(repo.description.asString(), style = MaterialTheme.typography.bodyMedium)
                    Text(repo.language.asString(), Modifier.padding(vertical = 8.dp))
                    Text(
                        stringResource(R.string.watchers_score, repo.watchers),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        stringResource(R.string.stars_score, repo.stars),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        stringResource(R.string.forks_score, repo.forks),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                HorizontalDivider()
            }
        }
    }
    if (state.showBlogDialog) {
        AlertDialog(
            onDismissRequest = { onAction(ReposAction.DismissBlogDialog) },
            title = { Text(stringResource(R.string.blog_not_found_title)) },
            text = { Text(stringResource(R.string.blog_not_found_text)) },
            confirmButton = {
                TextButton(onClick = {
                    onAction(ReposAction.DismissBlogDialog)
                }) { Text(stringResource(R.string.got_it)) }
            },
        )
    }
}

@Composable
private fun ProfileHeader(user: ProfileUi, onAction: (ReposAction) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(user.login, user.avatarUrl)
            Column(Modifier.padding(start = 16.dp)) {
                Text(stringResource(R.string.repo_score, user.publicRepos))
                Text(stringResource(R.string.followers_score, user.followers))
                Text(stringResource(R.string.following_score, user.following))
            }
        }
        Text(user.name, style = MaterialTheme.typography.titleLarge)
        Text(user.location.asString())
        Row {
            Button(onClick = {
                onAction(ReposAction.OpenProfile)
            }) { Text(stringResource(R.string.profile)) }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = {
                onAction(ReposAction.OpenBlog)
            }) { Text(stringResource(R.string.blog)) }
        }
    }
    HorizontalDivider(thickness = 8.dp)
}

@Preview(showBackground = true)
@Composable
private fun ReposScreenPreview() {
    GithubComposeTheme {
        ReposScreen(
            ReposState(
                user = ProfileUi(
                    "octocat", "", "The Octocat",
                    UiText.DynamicString(
                        "San Francisco",
                    ),
                    "", "https://github.com/octocat", "8", "100", "9",
                ),
                repos = listOf(
                    RepoUi(
                        1,
                        "Hello-World",
                        UiText.DynamicString("My first repository"),
                        UiText.DynamicString("KOTLIN"),
                        "12",
                        "30",
                        "4",
                        "https://github.com/octocat/Hello-World",
                    ),
                ),
            ),
            {},
        )
    }
}
