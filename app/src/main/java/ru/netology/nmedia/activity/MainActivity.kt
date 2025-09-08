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

    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + binding.main.paddingLeft,
                systemBars.top + binding.main.paddingTop,
                systemBars.right + binding.main.paddingRight,
                systemBars.bottom + binding.main.paddingBottom
            )
            insets
        }

        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                tvLike.text = viewModel.formatShortNumber(post.likes)
                tvShare.text = viewModel.formatShortNumber(post.shares)
                tvViews.text = viewModel.formatShortNumber(post.views)

                if (post.likedByMe) {
                    icLike.setImageResource(R.drawable.ic_round_favorite_24)
                }

                icLike.setImageResource(
                    if (post.likedByMe) R.drawable.ic_round_favorite_24
                    else R.drawable.ic_outline_favorite_border_24
                )
            }
        }

        binding.icLike.setOnClickListener {
            viewModel.like()
        }

        binding.icShare.setOnClickListener {
            viewModel.share()
        }
    }
}