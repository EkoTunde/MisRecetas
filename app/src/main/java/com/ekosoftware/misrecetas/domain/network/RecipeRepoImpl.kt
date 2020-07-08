package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.data.model.Recipe
import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

class RecipeRepoImpl(private val recipesDataSource: RecipesDataSource) : RecipeRepo {

    @ExperimentalCoroutinesApi
    override suspend fun getUserRecipes(): Flow<Resource<List<Recipe>>> = recipesDataSource.getAllRecipes()

    override suspend fun addRecipe(recipe: Recipe): Resource<Boolean> = addRecipe(recipe)

    override suspend fun updateRecipe(recipe: Recipe): Resource<Boolean> = updateRecipe(recipe)

    override suspend fun deleteRecipe(docId: String): Resource<Boolean> = deleteRecipe(docId)
}