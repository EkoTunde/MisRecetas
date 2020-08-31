package com.ekosoftware.misrecetas.di

import com.ekosoftware.misrecetas.data.network.UserDataSource
import com.ekosoftware.misrecetas.domain.network.UserRepo
import com.ekosoftware.misrecetas.domain.network.UserRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
class UserModule {

    @ActivityRetainedScoped
    @Provides
    fun provideUserDataSource(): UserDataSource {
        return UserDataSource()
    }

    @ActivityRetainedScoped
    @Provides
    fun provideUserRepo(userDataSource: UserDataSource): UserRepo {
        return UserRepoImpl(userDataSource)
    }
}