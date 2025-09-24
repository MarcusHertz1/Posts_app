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
import ru.netology.nmedia.activity.FeedFragment.Companion.textArgs
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.viewmodel.PostViewModel

class PostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)

        val postId = arguments?.postId ?: throw IllegalArgumentException("Post ID is required")

        val listener = object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_postFragment_to_newPostFragment,
                    Bundle().apply {
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
                findNavController().navigateUp()
            }

            override fun onPlayVideo(post: Post) {
                post.video?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }
            }

            override fun onPostClick(post: Post) {}
        }

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == postId } ?: return@observe
            val holder = PostViewHolder(binding.singlePost, listener, viewModel::formatShortNumber)
            holder.bind(post)
        }

        return binding.root
    }


    companion object {
        var Bundle.postId by LongArg
    }
}