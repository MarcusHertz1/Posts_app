package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostAdapter(
            object : OnInteractionListener {
                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                }

                override fun onShare(post: Post) {
                    viewModel.share(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val intent1 = Intent.createChooser(intent, getString(R.string.nmedia))

                    startActivity(intent1)
                }

                override fun onLike(post: Post) {
                    viewModel.like(post.id)
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }
            },
            formatNumber = { number ->
                viewModel.formatShortNumber(number)
            }
        )

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            val isNew = posts.size != adapter.itemCount
            adapter.submitList(posts) {
                if (isNew)
                    binding.list.smoothScrollToPosition(0)
            }
        }

        val newPostLauncher = registerForActivityResult(NewPostResultContract()) { content ->
            content ?: return@registerForActivityResult
            viewModel.changeContent(content)
            viewModel.save()
        }

        binding.fab.setOnClickListener {
            newPostLauncher.launch()
        }

        /*viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                with(binding.content) {
                    AndroidUtils.showKeyboard(this)
                    setText(post.content)
                    binding.editGroup.visibility = View.VISIBLE
                    binding.subtitleText.text = post.author
                }
            } else {
                with(binding) {
                    content.setText("")
                    content.clearFocus()
                    AndroidUtils.hideKeyboard(root)
                    editGroup.visibility = View.GONE
                    subtitleText.text = ""
                }
            }
        }

        binding.cancel.setOnClickListener {
            viewModel.edit(PostViewModel.empty)
        }

        with(binding) {
            save.setOnClickListener {
                if (content.text.isNullOrBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.error_empty_content,
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                viewModel.changeContent(content.text.toString())
                viewModel.save(content.text.toString())
                content.setText("")
                content.clearFocus()
                AndroidUtils.hideKeyboard(it)
                editGroup.visibility = View.GONE
            }
        }*/
    }
}