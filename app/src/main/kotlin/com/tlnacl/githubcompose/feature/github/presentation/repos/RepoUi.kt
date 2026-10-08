package com.tlnacl.githubcompose.feature.github.presentation.repos

import com.tlnacl.githubcompose.R
import com.tlnacl.githubcompose.core.presentation.UiText
import com.tlnacl.githubcompose.feature.github.domain.Repo
import java.util.Locale

data class RepoUi(
    val id: Long,
    val name: String,
    val description: UiText,
    val language: UiText,
    val watchers: String,
    val stars: String,
    val forks: String,
    val htmlUrl: String,
)

fun Repo.toRepoUi() = RepoUi(
    id = id,
    name = name,
    description = description?.let(UiText::DynamicString)
        ?: UiText.StringResource(R.string.no_description),
    language = language?.uppercase(Locale.getDefault())?.let(UiText::DynamicString)
        ?: UiText.StringResource(R.string.language_not_defined),
    watchers = watchersCount.toString(),
    stars = stargazersCount.toString(),
    forks = forksCount.toString(),
    htmlUrl = htmlUrl,
)
