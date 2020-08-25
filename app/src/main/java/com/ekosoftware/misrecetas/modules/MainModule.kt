package com.ekosoftware.misrecetas.modules

import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.domain.network.RecipeRepo
import com.ekosoftware.misrecetas.domain.network.RecipeRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
class MainModule {

    @ActivityRetainedScoped
    @Provides
    fun provideRecipeDataSource(): RecipesDataSource {
        return RecipesDataSource()
    }

    @ActivityRetainedScoped
    @Provides
    fun provideRecipeRepo(recipesDataSource: RecipesDataSource): RecipeRepo {
        return RecipeRepoImpl(recipesDataSource)
    }
}