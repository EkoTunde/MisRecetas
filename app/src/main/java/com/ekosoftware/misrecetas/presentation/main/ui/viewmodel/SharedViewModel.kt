package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ekosoftware.misrecetas.domain.model.Recipe

class SharedViewModel : ViewModel() {

    val actionableRecipe = MutableLiveData<Pair<RecipeAction,Recipe>>()

    fun addRecipe(recipe: Recipe) {
        actionableRecipe.postValue(RecipeAction.ADD to recipe)
    }

    fun editRecipe(recipe: Recipe) {
        actionableRecipe.postValue(RecipeAction.EDIT to recipe)
    }

    fun deleteRecipe(recipe: Recipe) {
        actionableRecipe.postValue(RecipeAction.DELETE to recipe)
    }
}

enum class RecipeAction {
    ADD, EDIT, DELETE
}