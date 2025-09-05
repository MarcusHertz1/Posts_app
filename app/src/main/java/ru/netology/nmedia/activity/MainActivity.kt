package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                tvLike.text = formatShortNumber(post.likes)
                tvShare.text = formatShortNumber(post.shares)
                tvViews.text = formatShortNumber(post.views)

                if (post.likedByMe) {
                    icLike.setImageResource(R.drawable.ic_round_favorite_24)
                }

                icLike.setImageResource(
                    if (post.likedByMe) R.drawable.ic_round_favorite_24
                    else R.drawable.ic_outline_favorite_border_24
                )

                icShare.setOnClickListener {
                    post.shares++
                    tvShare.text = formatShortNumber(post.shares)
                }
            }
        }
        binding.icLike.setOnClickListener {
            viewModel.like()
        }
    }

    fun formatShortNumber(value: Long): String {
        return when {
            value < 1_000 -> value.toString()
            value < 10_000 -> {
                val thousands = value / 1000
                val hundreds = (value % 1000) / 100
                "$thousands.${hundreds}K"
            }
            value < 1_000_000 -> {
                val thousands = value / 1000
                "${thousands}K"
            }
            else -> {
                val millions = value / 1_000_000
                val hundredThousands = (value % 1_000_000) / 100_000
                "$millions.${hundredThousands}M"
            }
        }
    }
}