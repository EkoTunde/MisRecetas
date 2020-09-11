package com.ekosoftware.misrecetas.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentLoginBinding
import com.ekosoftware.misrecetas.ui.home.HomeFragment
import com.ekosoftware.misrecetas.ui.viewmodel.UserViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val RC_SIGN_IN = 1
    }

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.exitBtn.setOnClickListener { requireActivity().finishAndRemoveTask() }
        binding.continueBtn.setOnClickListener { launchLoginFlow() }
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
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Receive signing in result from Firebase Auth UI
        if (requestCode == HomeFragment.RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                userViewModel.updateUser()
                val welcomeUser = getString(R.string.welcome_user, userViewModel.currentUser.displayName)
                Toast.makeText(requireContext(), welcomeUser, Toast.LENGTH_SHORT).show()

                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
            } else {
                // Error signing in
                Snackbar.make(binding.root, R.string.retry, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        launchLoginFlow()
                    }.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}