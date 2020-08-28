package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.data.network.RecipesDataSource
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecipeRepoImpl @Inject constructor(private val recipesDataSource: RecipesDataSource) : RecipeRepo {

    @ExperimentalCoroutinesApi
    override suspend fun getUserRecipes(): Flow<Resource<List<Recipe>>> = recipesDataSource.getAllRecipes()

    @ExperimentalCoroutinesApi
    override suspend fun getRecipes(filter: String): Flow<Resource<List<Recipe>>> = recipesDataSource.getRecipes(filter)

    override suspend fun addRecipe(recipe: Recipe): String = recipesDataSource.addRecipe(recipe)

    override suspend fun updateRecipe(recipe: Recipe): String = recipesDataSource.updateRecipe(recipe)

    override suspend fun deleteRecipe(recipe: Recipe): String = recipesDataSource.deleteRecipe(recipe)

    override suspend fun deleteImage(recipeId: String?, uuid: String) = recipesDataSource.deleteImage(recipeId, uuid)
}