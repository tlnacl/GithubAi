package com.tlnacl.githubcompose
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme
import com.tlnacl.githubcompose.feature.github.presentation.UsersRoute
import com.tlnacl.githubcompose.feature.github.presentation.githubGraph
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GithubComposeTheme { AppNavigation() } }
    }
}
@Composable
private fun AppNavigation() {
    val controller = rememberNavController()
    NavHost(controller, startDestination = UsersRoute) { githubGraph(controller) }
}
