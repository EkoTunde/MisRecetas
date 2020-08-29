package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ekosoftware.misrecetas.data.network.UploadImageWorker
import com.ekosoftware.misrecetas.data.network.UploadImageWorker.Companion.KEY_IMAGE_NAME
import com.ekosoftware.misrecetas.data.network.UploadImageWorker.Companion.KEY_IMAGE_URI
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.domain.network.RecipeRepo
import com.ekosoftware.misrecetas.util.FirebaseError
import com.ekosoftware.misrecetas.vo.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel @ViewModelInject constructor(
    @ApplicationContext appContext: Context,
    private val recipeRepo: RecipeRepo
) : ViewModel() {

    private val tag = "MainViewModel"

    var fetchRecipes = liveData(IO) {
        emit(Resource.Loading())
        try {
            recipeRepo.getUserRecipes().collect { result ->
                emit(result)
                Log.d(tag, "Result: $result ")
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    val eventResultReceiver = MutableLiveData<EventResult>()

    private fun publishEventResult(eventResult: EventResult) {
        eventResultReceiver.value = eventResult
    }

    fun startNetworkOperation(event: Event, recipe: Recipe) = viewModelScope.launch {
        publishEventResult(EventResult(event, Result.LOADING, recipe))
        try {
            val recipeName = when (event) {
                Event.ADD -> recipeRepo.addRecipe(recipe)
                Event.UPDATE -> recipeRepo.updateRecipe(recipe)
                Event.DELETE -> recipeRepo.deleteRecipe(recipe)
            }
            if (recipeName.isNotEmpty()) {
                //showRecipeDetails(recipe)
                publishEventResult(EventResult(event, Result.SUCCESS, recipe))
            } else throw IllegalArgumentException("Recipe name can't be null")
        } catch (e: Exception) {
            val failureMsg = if (e.message != null && e.message!!.contains("Missing or insufficient permissions")) {
                FirebaseError.ERROR_MISSING_PERMISSIONS
            } else FirebaseError.NONE
            publishEventResult(EventResult(event, Result.FAILURE, recipe, failureMsg))
        }
    }

    val detailRecipe = MutableLiveData<Recipe>()

    fun showRecipeDetails(recipe: Recipe) {
        detailRecipe.value = recipe
    }

    private val workManager = WorkManager.getInstance(appContext)

    private val uploadImageRequest = MutableLiveData<OneTimeWorkRequest>()

    private lateinit var uploadImageRequestId: UUID

    val uploadImageWork = uploadImageRequest.distinctUntilChanged().switchMap {
        workManager.enqueue(it)
        uploadImageRequestId = it.id
        workManager.getWorkInfoByIdLiveData(it.id)
    }

    fun uploadImage(imageUri: Uri, name: String) {

        val workSimulationWorkerRequest = OneTimeWorkRequestBuilder<UploadImageWorker>()
            .setInputData(createInputDataForUri(imageUri, name))
            .build()
        uploadImageRequest.value = workSimulationWorkerRequest
    }

    private fun createInputDataForUri(imageUri: Uri?, name: String): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, it.toString())
        }
        builder.putString(KEY_IMAGE_NAME, name)
        return builder.build()
    }

    fun cancelImageUpload() = uploadImageRequest.value?.id?.let {
        workManager.cancelWorkById(it)
    }

    fun deleteImage(recipeId: String?, uuid: String) = viewModelScope.launch { recipeRepo.deleteImage(recipeId, uuid) }
}

enum class Event {
    ADD, UPDATE, DELETE
}

enum class Result {
    LOADING, SUCCESS, FAILURE
}

data class EventResult(
    val event: Event,
    val result: Result,
    val recipe: Recipe,
    var failureMsg: FirebaseError = FirebaseError.NONE
)