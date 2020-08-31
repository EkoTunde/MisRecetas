package com.ekosoftware.misrecetas.domain.network

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseUser

interface UserRepo {
    suspend fun updateProfile(newDisplayName: String?, newPhotoUri: Uri?) : FirebaseUser
    suspend fun signOut(context: Context) : Void?
    suspend fun deleteUser(context: Context) : Void?
}