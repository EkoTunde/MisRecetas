package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.data.model.User
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseAuth

interface UserRepo {

    fun isUserLoggedIn() : Boolean

    suspend fun getUserData() : Resource<User>
}