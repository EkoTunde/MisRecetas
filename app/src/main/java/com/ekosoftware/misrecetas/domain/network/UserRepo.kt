package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.domain.model.User
import com.ekosoftware.misrecetas.vo.Resource

interface UserRepo {

    fun isUserLoggedIn() : Boolean

    suspend fun getUserData() : Resource<User>
}