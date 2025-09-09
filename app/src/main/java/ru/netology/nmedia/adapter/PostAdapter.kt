package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit
typealias FormatNumber = (number: Long) -> String

class PostAdapter(
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener,
    private val formatNumber: FormatNumber
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onLikeListener, onShareListener, formatNumber)
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
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener,
    private val formatNumber: FormatNumber
) : RecyclerView.ViewHolder(binding.root) {
    fun bind (post: Post) = with(binding) {
        author.text = post.author
        published.text = post.published
        content.text = post.content
        tvLike.text = formatNumber(post.likes)
        tvShare.text = formatNumber(post.shares)
        tvViews.text = formatNumber(post.views)

        if (post.likedByMe) {
            icLike.setImageResource(R.drawable.ic_round_favorite_24)
        }

        icLike.setImageResource(
            if (post.likedByMe) R.drawable.ic_round_favorite_24
            else R.drawable.ic_outline_favorite_border_24
        )

        icLike.setOnClickListener {
            onLikeListener(post)
        }

        icShare.setOnClickListener {
            onShareListener(post)
        }
    }
}

object PostDiffCallBack: DiffUtil.ItemCallback<Post>(){
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