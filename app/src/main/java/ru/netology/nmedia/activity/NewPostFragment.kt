package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.activity.FeedFragment.Companion.postId
import ru.netology.nmedia.activity.FeedFragment.Companion.textArgs
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue

class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            layoutInflater,
            container,
            false
        )

        val postId = arguments?.postId
        val content = arguments?.textArgs
        if (postId != null && content != null) {
            val post = viewModel.empty.copy(id = postId, content = content)
            viewModel.edit(post)
            binding.edit.setText(content)
        } else {
            viewModel.edit(viewModel.empty)
            viewModel.draft.value?.let { draft ->
                if (draft.isNotEmpty()) {
                    viewModel.changeContent(draft)
                    binding.edit.setText(draft)
                }
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentText = binding.edit.text.toString().trim()
                if (currentText.isNotEmpty() && viewModel.edited.value?.id == 0L) {
                    viewModel.saveDraft(currentText)
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.save.setOnClickListener {
            if (binding.edit.text.isNotBlank()) {
                val content = binding.edit.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
            }
            findNavController().navigateUp()
        }
        return binding.root
    }
}