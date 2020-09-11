package com.ekosoftware.misrecetas.data.network

import android.content.Context
import android.net.Uri
import com.ekosoftware.misrecetas.vo.Resource
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserDataSource {

    suspend fun updateProfile(newDisplayName: String?, newPhotoUri: Uri?) : FirebaseUser {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = newDisplayName
            photoUri = newPhotoUri
        }
        user!!.updateProfile(profileUpdates).await()
        return user
    }

    @ExperimentalCoroutinesApi
    suspend fun authState() = callbackFlow {
        val listener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null) offer(Resource.Success(false)) else offer(Resource.Success(true))
        }
        awaitClose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    suspend fun signOut(context: Context): Void? = AuthUI.getInstance().signOut(context).await()

    suspend fun deleteUser(context: Context): Void? = AuthUI.getInstance().delete(context).await()
}