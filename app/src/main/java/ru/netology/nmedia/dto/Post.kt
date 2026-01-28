package ru.netology.nmedia.dto

sealed interface FeedItem{
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorId: Long,
    val content: String,
    val published: String,
    val likes: Long,
    val likedByMe: Boolean = false,
    val shares: Long,
    val views: Long,
    val video: String? = null,
    val authorAvatar: String? = null,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String
): FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

/*
enum class AttachmentType {
    IMAGE, VIDEO
}*/
