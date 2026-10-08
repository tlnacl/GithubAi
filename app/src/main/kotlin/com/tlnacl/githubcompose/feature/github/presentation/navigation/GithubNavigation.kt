package com.tlnacl.githubcompose.feature.github.presentation.navigation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tlnacl.githubcompose.feature.github.presentation.repos.ReposRoot
import com.tlnacl.githubcompose.feature.github.presentation.users.UsersRoot
import kotlinx.serialization.Serializable

@Serializable data object UsersRoute

@Serializable data class ReposRoute(val login: String)
fun NavGraphBuilder.githubGraph(controller: NavController) {
    composable<UsersRoute> {
        UsersRoot(onNavigateToRepos = { controller.navigate(ReposRoute(it)) })
    }
    composable<ReposRoute> { ReposRoot(onBack = { controller.popBackStack() }) }
}
