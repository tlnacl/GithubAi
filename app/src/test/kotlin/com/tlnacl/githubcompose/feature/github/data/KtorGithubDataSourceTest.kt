package com.tlnacl.githubcompose.feature.github.data
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.tlnacl.githubcompose.BuildConfig
import com.tlnacl.githubcompose.core.data.HttpClientFactory
import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.Result
import com.tlnacl.githubcompose.feature.github.domain.Repo
import com.tlnacl.githubcompose.feature.github.domain.User
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class KtorGithubDataSourceTest {
    private fun engine(body: String, status: HttpStatusCode = HttpStatusCode.OK) = MockEngine {
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
    }

    @Test fun `users deserialize without profile fields or unknown fields`() = runTest {
        HttpClientFactory.create(
            engine(
                """[{"login":"octocat","avatar_url":"avatar","html_url":"profile","unknown":true}]""",
            ),
        ).use { client ->
            val result = KtorGithubDataSource(client).getUsers() as Result.Success<List<User>>
            assertThat(result.data.single().login).isEqualTo("octocat")
            assertThat(result.data.single().name).isEmpty()
        }
    }

    @Test fun `profile allows null name and blog`() = runTest {
        HttpClientFactory.create(
            engine("""{"login":"octocat","name":null,"blog":null,"public_repos":8}"""),
        ).use { client ->
            val result = KtorGithubDataSource(client).getUser("octocat") as Result.Success<User>
            assertThat(result.data.blogUrl).isEmpty()
            assertThat(result.data.publicRepos).isEqualTo(8)
        }
    }

    @Test fun `repository endpoint includes login and maps counters`() = runTest {
        val engine = MockEngine { request ->
            assertThat(
                request.url.toString(),
            ).isEqualTo("${BuildConfig.API_BASE_URL}users/octocat/repos")
            respond(
                """[{"id":1,"name":"Hello","stargazers_count":30,"forks_count":4}]""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        HttpClientFactory.create(engine).use { client ->
            val result = KtorGithubDataSource(
                client,
            ).getRepos("octocat") as Result.Success<List<Repo>>
            assertThat(result.data.single().stargazersCount).isEqualTo(30)
            assertThat(result.data.single().description).isNull()
        }
    }

    @Test fun `rate limit and not found have typed errors`() = runTest {
        for ((status, expected) in listOf(
            HttpStatusCode.TooManyRequests to DataError.Network.TOO_MANY_REQUESTS,
            HttpStatusCode.NotFound to DataError.Network.NOT_FOUND,
            HttpStatusCode.Forbidden to DataError.Network.FORBIDDEN,
        )) {
            HttpClientFactory.create(engine("{}", status)).use { client ->
                assertThat(
                    KtorGithubDataSource(client).getUsers(),
                ).isEqualTo(Result.Error(expected))
            }
        }
    }

    @Test fun `malformed successful body becomes serialization failure`() = runTest {
        HttpClientFactory.create(engine("not json")).use { client ->
            assertThat(
                KtorGithubDataSource(client).getUsers(),
            ).isEqualTo(Result.Error(DataError.Network.SERIALIZATION))
        }
    }

    @Test fun `connection failure becomes no internet`() = runTest {
        HttpClientFactory.create(MockEngine { throw IOException("offline") }).use { client ->
            assertThat(
                KtorGithubDataSource(client).getUsers(),
            ).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
        }
    }

    @Test fun `cancellation propagates instead of becoming an error`() = runTest {
        HttpClientFactory.create(
            MockEngine {
                throw CancellationException("cancelled")
            },
        ).use { client ->
            var cancelled = false
            try {
                KtorGithubDataSource(client).getUsers()
            } catch (
                e: CancellationException,
            ) {
                cancelled =
                    true
            }
            assertThat(cancelled).isTrue()
        }
    }
}
