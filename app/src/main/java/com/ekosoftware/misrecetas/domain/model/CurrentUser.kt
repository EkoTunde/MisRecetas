package com.ekosoftware.misrecetas.domain.model

object CurrentUser {

    private var user = User()

    fun updateUser(user: User) {
        this.user = user
    }

    val data get() = user
}