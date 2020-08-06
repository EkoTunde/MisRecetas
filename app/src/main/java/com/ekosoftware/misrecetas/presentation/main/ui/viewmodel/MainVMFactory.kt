package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ekosoftware.misrecetas.domain.network.RecipeRepo
import org.jetbrains.annotations.NotNull

class MainVMFactory(private val recipeRepo: RecipeRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(RecipeRepo::class.java).newInstance(recipeRepo)
    }
}