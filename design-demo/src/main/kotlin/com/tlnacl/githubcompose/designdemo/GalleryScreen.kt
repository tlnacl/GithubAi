package com.tlnacl.githubcompose.designdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.tlnacl.githubcompose.core.designsystem.Avatar
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme
import com.tlnacl.githubcompose.core.designsystem.Loading
import com.tlnacl.githubcompose.core.designsystem.UserRow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryState(val darkTheme: Boolean = false)
sealed interface GalleryAction {
    data class SetDarkTheme(val enabled: Boolean) : GalleryAction
    data object ClickSample : GalleryAction
}
sealed interface GalleryEvent {
    data object ShowFeedback : GalleryEvent
}
class GalleryViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val mutableState =
        MutableStateFlow(GalleryState(savedStateHandle["darkTheme"] ?: false))
    val state = mutableState.asStateFlow()
    private val eventChannel = Channel<GalleryEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: GalleryAction) {
        when (action) {
            is GalleryAction.SetDarkTheme -> {
                savedStateHandle["darkTheme"] = action.enabled
                mutableState.update { it.copy(darkTheme = action.enabled) }
            }

            GalleryAction.ClickSample -> viewModelScope.launch {
                eventChannel.send(GalleryEvent.ShowFeedback)
            }
        }
    }
}

@Composable
fun GalleryRoot(viewModel: GalleryViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    GalleryEvents(viewModel, snackbar)
    GithubComposeTheme(darkTheme = state.darkTheme) {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
            GalleryScreen(state, viewModel::onAction, Modifier.padding(padding))
        }
    }
}

@Composable
private fun GalleryEvents(viewModel: GalleryViewModel, snackbar: SnackbarHostState) {
    val owner = LocalLifecycleOwner.current
    val message = stringResource(R.string.action_feedback)
    LaunchedEffect(viewModel, owner, message) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    GalleryEvent.ShowFeedback -> snackbar.showSnackbar(message)
                }
            }
        }
    }
}

@Composable
fun GalleryScreen(
    state: GalleryState,
    onAction: (GalleryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.intro), style = MaterialTheme.typography.bodyLarge)
            Row(
                Modifier.fillMaxWidth().toggleable(
                    value = state.darkTheme,
                    role = Role.Switch,
                    onValueChange = { onAction(GalleryAction.SetDarkTheme(it)) },
                ).padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.dark_theme))
                Switch(checked = state.darkTheme, onCheckedChange = null)
            }
        }
        item {
            GallerySection(stringResource(R.string.colors)) {
                ColorSample(
                    stringResource(R.string.primary),
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                )
                ColorSample(
                    stringResource(R.string.secondary),
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary,
                )
                ColorSample(
                    stringResource(R.string.surface),
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        item {
            GallerySection(stringResource(R.string.typography)) {
                Text(
                    stringResource(R.string.headline),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(stringResource(R.string.title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.body), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.label), style = MaterialTheme.typography.labelSmall)
            }
        }
        item {
            GallerySection(stringResource(R.string.avatars)) {
                Avatar(login = "Alex", url = "")
                Text(
                    stringResource(R.string.avatar_note),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            GallerySection(stringResource(R.string.user_rows)) {
                UserRow(
                    "octocat",
                    "",
                    "https://github.com/octocat",
                    false,
                    false,
                    {
                        onAction(GalleryAction.ClickSample)
                    },
                    { onAction(GalleryAction.ClickSample) },
                )
                UserRow(
                    "a-long-user-login-for-layout-review",
                    "",
                    "https://github.com/a-long-user-login-for-layout-review",
                    true,
                    false,
                    {
                        onAction(GalleryAction.ClickSample)
                    },
                    { onAction(GalleryAction.ClickSample) },
                )
                UserRow("saving-user", "", "https://github.com/saving-user", false, true, {}, {})
            }
        }
        item {
            GallerySection(stringResource(R.string.loading_title)) {
                Loading(Modifier.fillMaxWidth().height(88.dp))
            }
        }
        item {
            GallerySection(stringResource(R.string.buttons)) {
                Text(
                    stringResource(R.string.button_note),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = {
                    onAction(GalleryAction.ClickSample)
                }) { Text(stringResource(R.string.primary_action)) }
                OutlinedButton(onClick = {
                    onAction(GalleryAction.ClickSample)
                }) { Text(stringResource(R.string.secondary_action)) }
                Button(onClick = {
                }, enabled = false) { Text(stringResource(R.string.disabled_action)) }
            }
        }
    }
}

@Composable
private fun GallerySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun ColorSample(label: String, color: Color, textColor: Color) {
    Text(
        label,
        color = textColor,
        modifier = Modifier.fillMaxWidth().background(
            color,
            MaterialTheme.shapes.medium,
        ).padding(16.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun GalleryLightPreview() {
    GithubComposeTheme(darkTheme = false) { Surface { GalleryScreen(GalleryState(), {}) } }
}

@Preview(showBackground = true)
@Composable
private fun GalleryDarkPreview() {
    GithubComposeTheme(darkTheme = true) {
        Surface { GalleryScreen(GalleryState(darkTheme = true), {}) }
    }
}
