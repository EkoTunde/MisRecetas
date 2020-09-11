package com.ekosoftware.misrecetas.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.databinding.FragmentProfileBinding
import com.ekosoftware.misrecetas.domain.model.User
import com.ekosoftware.misrecetas.ui.viewmodel.UserViewModel
import com.ekosoftware.misrecetas.util.GlideApp
import com.ekosoftware.misrecetas.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.theartofdev.edmodo.cropper.CropImage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    private val currentUser: User by lazy { userViewModel.currentUser }

    private var imageUrlHolder: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        populateUI()
        binding.btnEditImage.setOnClickListener { editImage() }
    }

    private fun initToolbar() {
        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbarProfile.apply {
            this.setupWithNavController(findNavController(), appBarConfiguration)
            title = requireContext().getString(R.string.account)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_save -> {
                        if (isContentChanged()) userViewModel.updateUser(getUserUpdates())
                        findNavController().navigateUp()
                        true
                    }
                    R.id.menu_item_sign_out -> {
                        userViewModel.singOut()
                        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
                        true
                    }
                    R.id.menu_item_delete_user -> {
                        userViewModel.revokeAccess()
                        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun populateUI() {
        binding.txtName.setText(currentUser.displayName)
        binding.txtEmail.setText(currentUser.email)
        binding.txtPhone.setText(currentUser.phoneNumber)
        GlideApp.with(this).load(currentUser.imageUrl).centerCrop().into(binding.profilePicture)
        fixUIFocus()
    }

    private fun fixUIFocus() {
        binding.txtNameLayout.isEndIconVisible = false
        binding.txtEmailLayout.isEndIconVisible = false
        binding.txtPhoneLayout.isEndIconVisible = false
        binding.profilePicture.requestFocus()
    }

    // Launch image selection
    private fun editImage() = CropImage.activity().start(requireActivity(), this@ProfileFragment)

    // To handle ImageCropperActivity's results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                CropImage.getActivityResult(data).uri.let { resultUri ->
                    GlideApp.with(this).load(resultUri).centerCrop().into(binding.profilePicture)
                    imageUrlHolder = resultUri.toString()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Snackbar.make(binding.root, R.string.error_getting_image, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { editImage() }
                    .show()
            }
        }
    }

    private fun isContentChanged() =
        currentUser.displayName != getUserUpdates().displayName || currentUser.imageUrl != getUserUpdates().imageUrl

    private fun getUserUpdates() = User(
        uid = currentUser.uid,
        displayName = binding.txtName.text.toString(),
        phoneNumber = currentUser.phoneNumber,
        email = currentUser.email,
        imageUrl = imageUrlHolder ?: currentUser.imageUrl
    )

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
        _binding = null
    }
}