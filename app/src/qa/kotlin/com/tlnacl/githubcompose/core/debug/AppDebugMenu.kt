package com.tlnacl.githubcompose.core.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tapadoo.debugmenu.DebugMenuOverlay
import com.tapadoo.debugmenu.module.DebugMenuModule
import com.tlnacl.githubcompose.BuildConfig
import com.tlnacl.githubcompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDebugMenu() {
    val title = stringResource(R.string.debug_app_title)
    val modules = remember(title) { listOf(AppInfoModule(title)) }
    DebugMenuOverlay(
        modules = modules,
        showFab = true,
        enableShake = false,
        colorScheme = MaterialTheme.colorScheme,
    )
}

private class AppInfoModule(override val title: String) : DebugMenuModule {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.debug_environment, BuildConfig.FLAVOR))
            Text(
                stringResource(
                    R.string.debug_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
            )
            Text(stringResource(R.string.debug_application_id, BuildConfig.APPLICATION_ID))
            Text(stringResource(R.string.debug_api_url, BuildConfig.API_BASE_URL))
        }
    }
}
