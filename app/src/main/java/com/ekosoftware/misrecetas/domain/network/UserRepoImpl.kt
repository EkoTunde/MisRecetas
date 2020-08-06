package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.domain.model.User
import com.ekosoftware.misrecetas.data.network.UsersDataSource
import com.ekosoftware.misrecetas.vo.Resource

class UserRepoImpl(private val usersDataSource: UsersDataSource) : UserRepo {

    override fun isUserLoggedIn(): Boolean = usersDataSource.checkIsUserLoggedIn()
    override suspend fun getUserData() : Resource<User> = usersDataSource.getUserData()
}