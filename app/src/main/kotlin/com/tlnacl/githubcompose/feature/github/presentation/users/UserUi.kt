package com.tlnacl.githubcompose.feature.github.presentation.users

import com.tlnacl.githubcompose.feature.github.domain.User

data class UserUi(val login: String, val avatarUrl: String, val htmlUrl: String)

fun User.toUserUi() = UserUi(login, avatarUrl, htmlUrl)
