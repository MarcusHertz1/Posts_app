package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.ErrorViewBinding
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.util.StringsArg
import ru.netology.nmedia.viewmodel.ViewModelFactory

class FeedFragment : Fragment() {

    private val dependencyContainer = DependencyContainer.getInstance()
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = {
            ViewModelFactory(
                repository = dependencyContainer.repository,
                appAuth = dependencyContainer.appAuth,
                apiService = dependencyContainer.apiService
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)

        val adapter = PostAdapter(
            object : OnInteractionListener {
                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    findNavController().navigate(
                        R.id.action_feedFragment_to_newPostFragment,
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
                    findNavController().navigate(
                        R.id.action_feedFragment_to_postFragment,
                        Bundle().apply {
                            postId = post.id
                        })
                }

                override fun onImageClick(imageUrl: String) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_viewingImagesFragment,
                        Bundle().apply {
                            this.imageUrl = imageUrl
                        })
                }
            },
            formatNumber = { number ->
                viewModel.formatShortNumber(number)
            },
            getAvatarUrl = { post ->
                viewModel.getAvatarUrl(post)
            },
            getImageUrl = { post ->
                viewModel.getImageUrl(post)
            }
        )

        binding.list.adapter = adapter

        val errorMergeBinding = ErrorViewBinding.bind(binding.root)

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.empty.isVisible = state.empty
        }

        viewModel.shouldScrollToTop.observe(viewLifecycleOwner) {
            binding.list.post {
                binding.list.smoothScrollToPosition(0)
            }
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            if (count != 0) {
                binding.newer.isVisible = true
                binding.newer.setOnClickListener {
                    binding.newer.isVisible = false
                    viewModel.loadNewerPosts()
                    binding.list.post {
                        binding.list.smoothScrollToPosition(0)
                    }
                }
            } else {
                binding.newer.isVisible = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    //binding.progress.isVisible = state.loading
                    errorMergeBinding.errorGroup.isVisible = state.error
                    binding.swipeRefresh.isRefreshing = state.loading
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
        }

        errorMergeBinding.retry.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root

    }

    companion object {
        var Bundle.textArgs by StringsArg
        var Bundle.postId by LongArg
        var Bundle.imageUrl by StringsArg
    }
}