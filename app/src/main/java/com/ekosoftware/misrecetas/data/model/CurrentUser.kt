package com.ekosoftware.misrecetas.data.model

object CurrentUser {

    private var user = User()

    fun updateUser(user: User) {
        this.user = user
    }

    val data get() = user
}