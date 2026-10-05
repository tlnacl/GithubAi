package com.tlnacl.githubcompose.core.data
import com.tlnacl.githubcompose.core.domain.DataError
import com.tlnacl.githubcompose.core.domain.Result
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException as JavaSocketTimeoutException

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> = try {
    val response = execute()
    when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        400 -> Result.Error(DataError.Network.BAD_REQUEST)
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        403 -> Result.Error(DataError.Network.FORBIDDEN)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
} catch (e: CancellationException) {
    throw e
} catch (e: HttpRequestTimeoutException) {
    Result.Error(DataError.Network.REQUEST_TIMEOUT)
} catch (e: ConnectTimeoutException) {
    Result.Error(DataError.Network.REQUEST_TIMEOUT)
} catch (e: SocketTimeoutException) {
    Result.Error(DataError.Network.REQUEST_TIMEOUT)
} catch (e: JavaSocketTimeoutException) {
    Result.Error(DataError.Network.REQUEST_TIMEOUT)
} catch (e: SerializationException) {
    Result.Error(DataError.Network.SERIALIZATION)
} catch (e: JsonConvertException) {
    Result.Error(DataError.Network.SERIALIZATION)
} catch (e: IOException) {
    Result.Error(DataError.Network.NO_INTERNET)
} catch (e: Exception) {
    Result.Error(DataError.Network.UNKNOWN)
}
