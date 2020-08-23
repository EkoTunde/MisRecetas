package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ekosoftware.misrecetas.domain.network.RecipeRepo

class MainVMFactory(private val application: Application, private val recipeRepo: RecipeRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Application::class.java, RecipeRepo::class.java).newInstance(application, recipeRepo)
    }
}