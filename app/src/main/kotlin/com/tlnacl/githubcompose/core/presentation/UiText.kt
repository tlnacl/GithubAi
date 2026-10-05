package com.tlnacl.githubcompose.core.presentation
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.domain.DataError

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(@param:StringRes val id: Int) : UiText
    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(id)
    }
}

@Composable fun UiText.asString(): String = asString(LocalContext.current)
fun DataError.toUiText(): UiText = UiText.StringResource(
    when (this) {
        DataError.Network.NO_INTERNET -> R.string.error_no_internet

        DataError.Network.REQUEST_TIMEOUT -> R.string.error_timeout

        DataError.Network.UNAUTHORIZED -> R.string.error_unauthorized

        DataError.Network.FORBIDDEN, DataError.Network.TOO_MANY_REQUESTS ->
            R.string.error_rate_limit

        DataError.Network.NOT_FOUND, DataError.Local.NOT_FOUND -> R.string.error_not_found

        DataError.Network.SERVER_ERROR, DataError.Network.SERVICE_UNAVAILABLE ->
            R.string.error_server

        DataError.Network.SERIALIZATION -> R.string.error_serialization

        DataError.Local.DISK_FULL -> R.string.error_disk_full

        else -> R.string.error_unknown
    },
)
