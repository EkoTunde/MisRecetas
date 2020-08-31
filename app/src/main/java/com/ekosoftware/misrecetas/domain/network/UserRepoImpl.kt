package com.ekosoftware.misrecetas.domain.network

import android.content.Context
import android.net.Uri
import com.ekosoftware.misrecetas.data.network.UserDataSource
import javax.inject.Inject

class UserRepoImpl @Inject constructor(private val dataSource: UserDataSource) : UserRepo {

    override suspend fun updateProfile(newDisplayName: String?, newPhotoUri: Uri?) =
        dataSource.updateProfile(newDisplayName, newPhotoUri)

    override suspend fun signOut(context: Context) = dataSource.signOut(context)
    override suspend fun deleteUser(context: Context) = dataSource.deleteUser(context)
}