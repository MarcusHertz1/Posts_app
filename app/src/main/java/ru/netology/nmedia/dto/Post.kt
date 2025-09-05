package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    var likes: Long,
    val likedByMe: Boolean = false,
    var shares: Long,
    var views: Long,
)
