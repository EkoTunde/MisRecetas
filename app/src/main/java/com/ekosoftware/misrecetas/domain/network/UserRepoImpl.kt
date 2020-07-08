package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.data.model.User
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.data.network.UsersDataSource
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepoImpl(private val usersDataSource: UsersDataSource) : UserRepo {

    override fun isUserLoggedIn(): Boolean = usersDataSource.checkIsUserLoggedIn()
    override suspend fun getUserData() : Resource<User> = usersDataSource.getUserData()
}