package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import androidx.lifecycle.*
import com.ekosoftware.misrecetas.domain.model.CurrentUser
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.domain.network.RecipeRepo
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers.IO

class MainViewModel(
    private val recipeRepo: RecipeRepo
) : ViewModel() {

    val currentUser = CurrentUser.data

    var fetchRecipes = liveData(IO) {
        emit(Resource.Loading())
        try {
            recipeRepo.getUserRecipes().collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    private val recipeEvent = MutableLiveData<RecipeEvent>()

    fun registerEvent(RecipeEvent: RecipeEvent) {
        recipeEvent.value = RecipeEvent
    }

    val fetchEvents = recipeEvent.distinctUntilChanged().switchMap { recipeEvent ->
        liveData(IO) {
            emit(Resource.Loading())
            try {
                val result = when (recipeEvent.event) {
                    Event.ADD -> recipeRepo.addRecipe(recipeEvent.recipe)
                    Event.UPDATE -> recipeRepo.addRecipe(recipeEvent.recipe)
                    Event.DELETE -> recipeRepo.deleteRecipe(recipeEvent.recipe)
                }
                emit(result)
            } catch (e: Exception) {
                emit(Resource.Failure(e))
            }
        }

    }

    fun addRecipe(recipe: Recipe) = liveData(IO) {
        try {
            val result = recipeRepo.addRecipe(recipe)
            emit(result)
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun updateRecipe(recipe: Recipe) = liveData(IO) {
        emit(Resource.Loading())
        try {
            val result = recipeRepo.updateRecipe(recipe)
            emit(result)
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun deleteRecipe(recipe: Recipe) = liveData(IO) {
        emit(Resource.Loading())
        try {
            val result = recipeRepo.deleteRecipe(recipe)
            emit(result)
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }
}

enum class Event {
    ADD, UPDATE, DELETE
}

data class RecipeEvent(
    val recipe: Recipe,
    val event: Event
)
