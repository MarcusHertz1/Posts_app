package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.databinding.FragmentViewingImagesBinding
import ru.netology.nmedia.util.StringsArg

class ViewingImagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentViewingImagesBinding.inflate(layoutInflater, container, false)

        val imageUrl = arguments?.imageUrl
        if (imageUrl != null) {
            Glide.with(binding.image.context)
                .load(imageUrl)
                .fitCenter()
                .timeout(10_000)
                .into(binding.image)
        }

        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    companion object {
        var Bundle.imageUrl by StringsArg
    }
}
