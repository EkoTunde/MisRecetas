package com.ekosoftware.misrecetas.presentation.main.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ekosoftware.misrecetas.domain.network.RecipeRepo
import com.ekosoftware.misrecetas.domain.network.UserRepo
import com.ekosoftware.misrecetas.vo.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers.IO

class HomeViewModel(private val userRepo: UserRepo, private val recipeRepo: RecipeRepo) :
    ViewModel() {

    val isUserLoggedIn get() = userRepo.isUserLoggedIn()

    var recipes = liveData(IO) {
        emit(Resource.Loading())
        try {
            recipeRepo.getUserRecipes().collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }


}