package com.tlnacl.githubcompose.feature.github.domain
import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.Result

data class User(
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val name: String = "",
    val location: String? = null,
    val blogUrl: String = "",
    val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
)
data class Repo(
    val id: Long,
    val name: String,
    val description: String?,
    val watchersCount: Int,
    val forksCount: Int,
    val stargazersCount: Int,
    val language: String?,
    val htmlUrl: String,
)
interface GithubRemoteDataSource {
    suspend fun getUsers(): Result<List<User>, DataError.Network>
    suspend fun getUser(login: String): Result<User, DataError.Network>
    suspend fun getRepos(login: String): Result<List<Repo>, DataError.Network>
}
