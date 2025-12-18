package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.postId
import ru.netology.nmedia.activity.FeedFragment.Companion.textArgs
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider

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

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.image_pick_error), Toast.LENGTH_LONG
                    ).show()
                    return@registerForActivityResult
                }

                val uri = result.data?.data ?: return@registerForActivityResult
                viewModel.updatePhoto(uri, uri.toFile())
            }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            if (binding.edit.text.isNotBlank()) {
                                val content = binding.edit.text.toString()
                                viewModel.changeContent(content)
                                viewModel.save()
                            }
                            findNavController().navigateUp()
                            true
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner
        )

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo == null){
                binding.previewContainer.isGone = true
                return@observe
            }

            binding.preview.setImageURI(photo.uri)
            binding.previewContainer.isVisible = true
        }

        binding.remove.setOnClickListener {
            viewModel.removePhoto()
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent (photoLauncher :: launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent (photoLauncher :: launch)
        }

        /*binding.save.setOnClickListener {
            if (binding.edit.text.isNotBlank()) {
                val content = binding.edit.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
            }
            findNavController().navigateUp()
        }*/

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }
}