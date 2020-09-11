package com.ekosoftware.misrecetas.domain.network

import android.content.Context
import android.net.Uri
import com.ekosoftware.misrecetas.data.network.UserDataSource
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepoImpl @Inject constructor(private val dataSource: UserDataSource) : UserRepo {

    override suspend fun updateProfile(newDisplayName: String?, newPhotoUri: Uri?) =
        dataSource.updateProfile(newDisplayName, newPhotoUri)

    override suspend fun signOut(context: Context) = dataSource.signOut(context)
    override suspend fun deleteUser(context: Context) = dataSource.deleteUser(context)
    @ExperimentalCoroutinesApi
    override suspend fun authState(): Flow<Resource<Boolean>> = dataSource.authState()
}