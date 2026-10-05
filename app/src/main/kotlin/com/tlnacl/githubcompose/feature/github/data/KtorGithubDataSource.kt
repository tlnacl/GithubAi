package com.tlnacl.githubcompose.feature.github.data
import com.tlnacl.githubcompose.core.data.safeCall
import com.tlnacl.githubcompose.core.domain.map
import com.tlnacl.githubcompose.feature.github.domain.GithubRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.appendPathSegments
import javax.inject.Inject
import javax.inject.Singleton

class KtorGithubDataSource @Inject constructor(private val client: HttpClient) : GithubRemoteDataSource {
    override suspend fun getUsers() = safeCall<List<UserDto>> { client.get("users") }
        .map { users -> users.map(UserDto::toUser) }
    override suspend fun getUser(login: String) = safeCall<UserDto> {
        client.get { url { appendPathSegments("users", login) } }
    }.map(UserDto::toUser)
    override suspend fun getRepos(login: String) = safeCall<List<RepoDto>> {
        client.get { url { appendPathSegments("users", login, "repos") } }
    }.map { repos -> repos.map(RepoDto::toRepo) }
}
@Module
@InstallIn(SingletonComponent::class)
abstract class GithubDataModule {
    @Binds @Singleton
    abstract fun bindGithubRemoteDataSource(source: KtorGithubDataSource): GithubRemoteDataSource
}
