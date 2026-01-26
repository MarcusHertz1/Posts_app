package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.viewmodel.SignInViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

class SignInFragment : Fragment() {
    private val dependencyContainer = DependencyContainer.getInstance()
    private val viewModel: SignInViewModel by viewModels(
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
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.signIn.setOnClickListener {
            val login = binding.login.text?.toString()?.trim().orEmpty()
            val pass = binding.password.text?.toString()?.trim().orEmpty()

            if (login.isBlank() || pass.isBlank()) {
                Toast.makeText(requireContext(), "Введите логин и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.authenticate(login, pass)
        }

        viewModel.authSuccess.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.authError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
}
