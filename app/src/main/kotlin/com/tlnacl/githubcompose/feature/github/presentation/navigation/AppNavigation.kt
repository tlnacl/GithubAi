package com.tlnacl.githubcompose.feature.github.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tlnacl.githubcompose.R

@Composable
fun AppNavigation() {
    val controller = rememberNavController()
    val entry by controller.currentBackStackEntryAsState()
    val destination = entry?.destination
    val home = destination?.hasRoute<UsersRoute>() == true
    val favorite = destination?.hasRoute<FavoritesRoute>() == true
    val setting = destination?.hasRoute<SettingsRoute>() == true
    val showBar = home || favorite || setting
    Scaffold(
        contentWindowInsets = if (showBar) WindowInsets.safeDrawing else WindowInsets(0),
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = home,
                        onClick = {
                            if (!home) {
                                controller.navigate(UsersRoute) {
                                    popUpTo<UsersRoute> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(painterResource(R.drawable.nav_home), null) },
                        label = { Text(stringResource(R.string.home)) },
                    )
                    NavigationBarItem(
                        selected = favorite,
                        onClick = {
                            if (!favorite) {
                                controller.navigate(FavoritesRoute) {
                                    popUpTo<UsersRoute> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(painterResource(R.drawable.nav_favorite), null) },
                        label = { Text(stringResource(R.string.favorite)) },
                    )
                    NavigationBarItem(
                        selected = setting,
                        onClick = {
                            if (!setting) {
                                controller.navigate(SettingsRoute) {
                                    popUpTo<UsersRoute> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(painterResource(R.drawable.nav_settings), null) },
                        label = { Text(stringResource(R.string.setting)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            controller,
            startDestination = UsersRoute,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            githubGraph(controller)
        }
    }
}
