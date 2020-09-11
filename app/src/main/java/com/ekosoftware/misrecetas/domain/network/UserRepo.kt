package com.ekosoftware.misrecetas.domain.network

import android.content.Context
import android.net.Uri
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface UserRepo {
    suspend fun updateProfile(newDisplayName: String?, newPhotoUri: Uri?) : FirebaseUser
    suspend fun signOut(context: Context) : Void?
    suspend fun deleteUser(context: Context) : Void?
    suspend fun authState() : Flow<Resource<Boolean>>
}