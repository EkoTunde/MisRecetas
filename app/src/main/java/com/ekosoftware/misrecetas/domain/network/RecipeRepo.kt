package com.ekosoftware.misrecetas.domain.network

import com.ekosoftware.misrecetas.data.model.Recipe
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.coroutines.flow.Flow

interface RecipeRepo {
    suspend fun getUserRecipes() : Flow<Resource<List<Recipe>>>
    suspend fun addRecipe(recipe: Recipe) : Resource<Boolean>
    suspend fun updateRecipe(recipe: Recipe) : Resource<Boolean>
    suspend fun deleteRecipe(docId: String) : Resource<Boolean>
}