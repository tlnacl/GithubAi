package com.tlnacl.githubcompose.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun UserRow(
    login: String,
    avatarUrl: String,
    profileUrl: String,
    isFavorite: Boolean,
    isPending: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(login, avatarUrl)
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(
                login,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                profileUrl,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconToggleButton(checked = isFavorite, enabled = !isPending, onCheckedChange = {
            onFavoriteClick()
        }) {
            Icon(
                painterResource(
                    if (isFavorite) R.drawable.heart_filled else R.drawable.heart_outline,
                ),
                stringResource(
                    if (isFavorite) R.string.remove_favorite else R.string.add_favorite,
                    login,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteUserRowPreview() {
    GithubComposeTheme { UserRow("octocat", "", "https://github.com/octocat", true, false, {}, {}) }
}
