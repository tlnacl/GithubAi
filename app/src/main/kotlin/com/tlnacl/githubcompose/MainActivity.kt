package com.tlnacl.githubcompose
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tlnacl.githubcompose.core.debug.AppDebugMenu
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme
import com.tlnacl.githubcompose.feature.github.presentation.navigation.UsersRoute
import com.tlnacl.githubcompose.feature.github.presentation.navigation.githubGraph
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GithubComposeTheme {
                Box {
                    AppNavigation()
                    AppDebugMenu()
                }
            }
        }
    }
}

@Composable
private fun AppNavigation() {
    val controller = rememberNavController()
    NavHost(controller, startDestination = UsersRoute) { githubGraph(controller) }
}
