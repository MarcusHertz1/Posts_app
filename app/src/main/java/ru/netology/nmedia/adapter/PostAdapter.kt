package ru.netology.nmedia.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

typealias FormatNumber = (number: Long) -> String
typealias GetAvatarUrl = (post: Post) -> String?
typealias GetImageUrl = (post: Post) -> String?

interface OnInteractionListener{
    fun onLike (post: Post)
    fun onRemove (post: Post)
    fun onEdit (post: Post)
    fun onShare (post: Post)
    fun onPlayVideo (post: Post)
    fun onPostClick (post: Post) {}
    fun onImageClick (imageUrl: String) {}
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val formatNumber: FormatNumber,
    private val getAvatarUrl: GetAvatarUrl,
    private val getImageUrl: GetImageUrl,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(
            binding,
            onInteractionListener,
            formatNumber,
            getAvatarUrl,
            getImageUrl
        )
    }

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val formatNumber: FormatNumber,
    private val getAvatarUrl: GetAvatarUrl,
    private val getImageUrl: GetImageUrl,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) = binding.apply {
        author.text = post.author
        published.text = post.published
        content.text = post.content
        icShare.text = formatNumber(post.shares)
        icViews.text = formatNumber(post.views)
        icLike.apply {
            isChecked = post.likedByMe
            text = formatNumber(post.likes)
        }
        menu.isVisible = post.ownedByMe

        val avatarUrl = getAvatarUrl(post)

        Log.d("AvatarDebug", "post=${post.id}, avatarUrl=$avatarUrl")
        Glide.with(avatar.context)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_netology)
            .error(R.drawable.ic_netology)
            .transform(CircleCrop())
            .timeout(10_000)
            .into(avatar)

        val imageUrl = getImageUrl(post)

        Log.d("AvatarDebug", "post=${post.id}, imageUrl=$imageUrl")
        if (imageUrl != null) {
            Glide.with(ivImagePreview.context)
                .load(imageUrl)
                .fitCenter()
                .timeout(10_000)
                .into(ivImagePreview)
            imagePreview.visibility = View.VISIBLE
            ivImagePreview.setOnClickListener {
                onInteractionListener.onImageClick(imageUrl)
            }
        } else {
            imagePreview.visibility = View.GONE
        }
        
        videoPreview.setOnClickListener {
            onInteractionListener.onPlayVideo(post)
        }

        content.setOnClickListener {
            onInteractionListener.onPostClick(post)
        }

        icLike.setOnClickListener {
            onInteractionListener.onLike(post)
        }

        icShare.setOnClickListener {
            onInteractionListener.onShare(post)
        }

        menu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.post_options)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onInteractionListener.onRemove(post)
                            true
                        }

                        R.id.edit -> {
                            onInteractionListener.onEdit(post)
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }
    }
}

object PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem == newItem
    }

}