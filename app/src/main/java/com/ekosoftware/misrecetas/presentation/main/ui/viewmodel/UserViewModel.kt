package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.ekosoftware.misrecetas.R
import com.ekosoftware.misrecetas.domain.constants.AuthOperation
import com.ekosoftware.misrecetas.domain.model.CurrentUser
import com.ekosoftware.misrecetas.domain.model.User
import com.ekosoftware.misrecetas.domain.network.UserRepoImpl
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO

class UserViewModel @ViewModelInject constructor(
    @ApplicationContext private val appContext: Context,
    private val userRepo: UserRepoImpl
) : ViewModel() {

    fun setUser() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            CurrentUser.updateUser(User(user.uid, user.displayName, user.phoneNumber, user.email, user.photoUrl.toString()))
        }
    }

    fun user() = CurrentUser.data

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

    private val authOperation = MutableLiveData<AuthOperation>()

    fun runAuthOperation(operation: AuthOperation) {
        authOperation.value = operation
    }

    private val TAG = "UserViewModel"
    val authOperationLiveData = authOperation.distinctUntilChanged().switchMap {
        liveData(IO) {
            try {
                when (it) {
                    AuthOperation.SIGN_OUT -> userRepo.signOut(appContext)
                    else -> userRepo.deleteUser(appContext)
                }
                emit(Resource.Success(it))
            } catch (e: Exception) {
                Log.d(TAG, "error: $e")
                emit(Resource.Failure(e))
            }
        }
    }
}