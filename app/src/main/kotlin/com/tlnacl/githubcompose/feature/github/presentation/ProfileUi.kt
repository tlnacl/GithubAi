package com.tlnacl.githubcompose.feature.github.presentation

import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.presentation.UiText
import com.tlnacl.githubcompose.feature.github.domain.User

data class ProfileUi(
    val login: String,
    val avatarUrl: String,
    val name: String,
    val location: UiText,
    val blogUrl: String,
    val htmlUrl: String,
    val publicRepos: String,
    val followers: String,
    val following: String,
)

fun User.toProfileUi() = ProfileUi(
    login = login,
    avatarUrl = avatarUrl,
    name = name.ifBlank { login },
    location = location?.takeIf(String::isNotBlank)?.let(UiText::DynamicString)
        ?: UiText.StringResource(R.string.location_not_defined),
    blogUrl = blogUrl,
    htmlUrl = htmlUrl,
    publicRepos = publicRepos.toString(),
    followers = followers.toString(),
    following = following.toString(),
)
