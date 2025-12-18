package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Long,
    val likedByMe: Boolean = false,
    val shares: Long,
    val views: Long,
    val video: String? = null,
    val authorAvatar: String? = null,
    @Embedded
    val attachment: Attachment?
){
    fun toDto() = Post(
        id = id,
        author = author,
        content = content,
        published = published,
        likes = likes,
        likedByMe = likedByMe,
        shares = shares,
        views = views,
        video = video,
        authorAvatar = authorAvatar,
        attachment = attachment
    )

    companion object {
        fun fromDto(post: Post) = post.run {
            PostEntity(
                id = id,
                author = author,
                content = content,
                published = published,
                likes = likes,
                likedByMe = likedByMe,
                shares = shares,
                views = views,
                video = video,
                authorAvatar = authorAvatar,
                attachment = attachment
            )
        }
    }
}
