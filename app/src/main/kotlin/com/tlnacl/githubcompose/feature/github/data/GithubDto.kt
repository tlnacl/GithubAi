package com.tlnacl.githubcompose.feature.github.data
import com.tlnacl.githubcompose.feature.github.domain.Repo
import com.tlnacl.githubcompose.feature.github.domain.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserSearchDto(val items: List<UserDto>)

@Serializable
internal data class UserDto(
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    val name: String? = null,
    val location: String? = null,
    @SerialName("blog") val blogUrl: String? = null,
    @SerialName("public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
)
internal fun UserDto.toUser() = User(
    login, avatarUrl, htmlUrl, name.orEmpty(), location,
    blogUrl.orEmpty(), publicRepos, followers, following,
)

@Serializable
internal data class RepoDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    @SerialName("watchers_count") val watchersCount: Int = 0,
    @SerialName("forks_count") val forksCount: Int = 0,
    @SerialName("stargazers_count") val stargazersCount: Int = 0,
    val language: String? = null,
    @SerialName("html_url") val htmlUrl: String = "",
)
internal fun RepoDto.toRepo() = Repo(
    id,
    name,
    description,
    watchersCount,
    forksCount,
    stargazersCount,
    language,
    htmlUrl,
)
