package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ekosoftware.misrecetas.domain.model.CurrentUser
import com.ekosoftware.misrecetas.domain.model.User
import com.google.firebase.auth.FirebaseAuth

class UserViewModel : ViewModel() {

    companion object {
        private val USER = FirebaseAuth.getInstance().currentUser!!
    }

    fun setUser() {
        CurrentUser.updateUser(
            User(
                USER.uid, USER.displayName, USER.phoneNumber, USER.email, USER.photoUrl.toString()
            )
        )
    }

}