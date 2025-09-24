package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.util.StringsArg

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)

        val adapter = PostAdapter(object : OnInteractionListener {
                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply {
                            postId = post.id
                            textArgs = post.content
                        })
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

                override fun onPlayVideo(post: Post) {
                    post.video?.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        startActivity(intent)
                    }
                }

                override fun onPostClick(post: Post) {
                    findNavController().navigate(R.id.action_feedFragment_to_postFragment,
                        Bundle().apply {
                            postId = post.id
                        })
                }
            },
            formatNumber = { number ->
                viewModel.formatShortNumber(number)
            }
        )

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val isNew = posts.size != adapter.itemCount
            adapter.submitList(posts) {
                if (isNew)
                    binding.list.smoothScrollToPosition(0)
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root

    }

    companion object{
        var Bundle.textArgs by StringsArg
        var Bundle.postId by LongArg
    }
}