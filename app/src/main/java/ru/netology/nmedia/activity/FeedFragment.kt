package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.ErrorViewBinding
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.util.StringsArg
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    val authViewModel: AuthViewModel by viewModels()


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

        lifecycleScope.launch {
            viewModel.data.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        viewModel.shouldScrollToTop.observe(viewLifecycleOwner) {
            binding.list.post {
                binding.list.smoothScrollToPosition(0)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    //binding.progress.isVisible = state.loading
                    errorMergeBinding.errorGroup.isVisible = state.error
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        errorMergeBinding.retry.setOnClickListener {
            //viewModel.loadPosts()
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

        return binding.root

    }

    companion object {
        var Bundle.textArgs by StringsArg
        var Bundle.postId by LongArg
        var Bundle.imageUrl by StringsArg
    }
}