package ru.netology.nmedia.model

data class FeedModelState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false
)