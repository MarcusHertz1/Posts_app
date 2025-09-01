package ru.netology.nmedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            likes = 10,
            likedByMe = false,
            shares = 1_848,
            views = 1_000_000
        )

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

            icLike.setOnClickListener {
                post.likedByMe = !post.likedByMe
                icLike.setImageResource(
                    if (post.likedByMe) R.drawable.ic_round_favorite_24
                    else R.drawable.ic_outline_favorite_border_24
                )
                if (post.likedByMe) post.likes++ else post.likes--
                tvLike.text = formatShortNumber(post.likes)
            }

            icShare.setOnClickListener {
                post.shares++
                tvShare.text = formatShortNumber(post.shares)
            }
        }
    }

    fun formatShortNumber(value: Long): String {
        return when {
            value < 1_000 -> value.toString()
            value < 10_000 -> {
                val short = value / 1000.0
                String.format(Locale.US, "%.1fK", short)
            }

            value < 1_000_000 -> {
                val thousands = value / 1000
                "${thousands}K"
            }

            else -> {
                val short = value / 1_000_000.0
                String.format(Locale.US, "%.1fM", short)
            }
        }
    }
}
