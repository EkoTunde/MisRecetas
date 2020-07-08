package com.ekosoftware.misrecetas.presentation.login.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentLoginBinding
import com.ekosoftware.misrecetas.presentation.main.MainActivity
import com.ekosoftware.misrecetas.presentation.main.ui.home.HomeFragment
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val RC_SIGN_IN = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.exitBtn.setOnClickListener {
            requireActivity().finishAndRemoveTask()
        }

        binding.continueBtn.setOnClickListener {
            launchLoginFlow()
        }
    }

    // Launches an intent to Firebase Auth UI Activity for handling user login
    private fun launchLoginFlow() {

        // Define which providers will be use from Firebase Authentication
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Receive signing in result from Firebase Auth UI
        if (requestCode == HomeFragment.RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser!!
                val welcomeUser = getString(R.string.welcome_user, user.displayName)
                Toast.makeText(requireContext(), welcomeUser, Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireActivity(), MainActivity::class.java))
            } else {
                // Error signing in
                Snackbar.make(binding.root, R.string.retry, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        launchLoginFlow()
                        val snack = it as Snackbar
                        snack.dismiss()
                    }.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}