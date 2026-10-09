package com.tlnacl.githubcompose.feature.github.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme

@Composable
fun SettingsRoot() {
    SettingsScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(stringResource(R.string.setting))
        }, windowInsets = WindowInsets(0))
        Text(stringResource(R.string.settings_coming_soon), Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    GithubComposeTheme { SettingsScreen() }
}
