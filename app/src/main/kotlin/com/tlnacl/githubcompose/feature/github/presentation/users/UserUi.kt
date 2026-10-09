package com.tlnacl.githubcompose.feature.github.presentation.users

import com.tlnacl.githubcompose.feature.github.domain.User

data class UserUi(
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val isFavorite: Boolean = false,
)

fun User.toUserUi() = UserUi(login, avatarUrl, htmlUrl)

fun com.tlnacl.githubcompose.feature.github.domain.FavoriteUser.toUserUi() =
    UserUi(login, avatarUrl, htmlUrl, isFavorite = true)
