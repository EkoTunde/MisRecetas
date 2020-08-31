package com.ekosoftware.misrecetas.data.network

import android.content.Context
import android.net.Uri
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
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

    suspend fun signOut(context: Context): Void? = AuthUI.getInstance().signOut(context).await()

    suspend fun deleteUser(context: Context): Void? = AuthUI.getInstance().delete(context).await()
}