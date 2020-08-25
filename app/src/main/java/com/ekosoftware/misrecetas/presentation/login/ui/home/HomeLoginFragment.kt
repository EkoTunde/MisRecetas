package com.ekosoftware.misrecetas.presentation.login.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentHomeLoginBinding
import com.ekosoftware.misrecetas.presentation.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

// Only used for displaying a ProgressBar
class HomeLoginFragment : Fragment() {
    private var _binding: FragmentHomeLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseAuth.getInstance().currentUser?.let {
            startActivity(Intent(requireActivity(), MainActivity::class.java))
        }
        findNavController().navigate(R.id.loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}