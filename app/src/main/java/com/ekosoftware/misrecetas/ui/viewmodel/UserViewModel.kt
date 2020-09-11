package com.ekosoftware.misrecetas.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.domain.model.CurrentUser
import com.ekosoftware.misrecetas.domain.model.User
import com.ekosoftware.misrecetas.domain.network.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class UserViewModel @ViewModelInject constructor(
    @ApplicationContext private val appContext: Context,
    private val userRepo: UserRepoImpl
) : ViewModel() {

    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser?.let {
            updateUser()
            true
        } ?: false
    }

    fun updateUser() = CurrentUser.updateUser(
        User(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.displayName,
            FirebaseAuth.getInstance().currentUser!!.phoneNumber,
            FirebaseAuth.getInstance().currentUser!!.email,
            FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()
        )
    )

    val currentUser get() = CurrentUser.data

    private val profileUpdateRequest = MutableLiveData<User>()

    fun updateUser(user: User) {
        profileUpdateRequest.value = user
    }

    val updateProfileLiveData = profileUpdateRequest.distinctUntilChanged().switchMap { user ->
        liveData(IO) {
            try {
                val result = userRepo.updateProfile(user.displayName, Uri.parse(user.imageUrl))
                val updatedUser = user.apply {
                    displayName = result.displayName
                    imageUrl = result.photoUrl.toString()
                }
                CurrentUser.updateUser(updatedUser)
                val msg = appContext.getString(R.string.successfully_updated_profile)
                emit(msg)
            } catch (e: Exception) {
                emit(appContext.getString(R.string.error_while_updating_profile))
            }
        }
    }

    fun singOut() = viewModelScope.launch {
        userRepo.signOut(appContext)
    }

    fun revokeAccess() = viewModelScope.launch {
        userRepo.deleteUser(appContext)
    }
}