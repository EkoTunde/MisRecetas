package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.coroutines.flow.Flow

interface RecipeRepo {
    suspend fun getUserRecipes() : Flow<Resource<List<Recipe>>>
    suspend fun getRecipes(filter: String) : Flow<Resource<List<Recipe>>>
    suspend fun addRecipe(recipe: Recipe) : String
    suspend fun updateRecipe(recipe: Recipe) : String
    suspend fun deleteRecipe(recipe: Recipe) : String
}