package com.tlnacl.githubcompose
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import com.tlnacl.githubcompose.core.debug.AppDebugMenu
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme
import com.tlnacl.githubcompose.feature.github.presentation.navigation.AppNavigation
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
