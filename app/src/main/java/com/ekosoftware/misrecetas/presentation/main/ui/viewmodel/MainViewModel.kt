package com.ekosoftware.misrecetas.presentation.main.ui.viewmodel

import android.app.Application
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    application: Application,
    private val recipeRepo: RecipeRepo
) : ViewModel() {

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

    val eventResultReceiver = MutableLiveData<EventResult>()

    private fun publishEventResult(eventResult: EventResult) {
        eventResultReceiver.value = eventResult
    }

    fun startNetworkOperation(event: Event, recipe: Recipe) = viewModelScope.launch {
        publishEventResult(EventResult(event, Result.LOADING, recipe))
        try {
            val recipeName = when (event) {
                Event.ADD -> recipeRepo.addRecipe(recipe)
                Event.UPDATE -> recipeRepo.addRecipe(recipe)
                Event.DELETE -> recipeRepo.deleteRecipe(recipe)
            }
            if (recipeName.isNotEmpty()) publishEventResult(EventResult(event, Result.SUCCESS, recipe))
            else throw IllegalArgumentException("Recipe name can't be null")
        } catch (e: Exception) {
            val failureMsg = if (e.message != null && e.message!!.contains("Missing or insufficient permissions")) {
                FirebaseError.ERROR_MISSING_PERMISSIONS
            } else FirebaseError.NONE
            publishEventResult(EventResult(event, Result.FAILURE, recipe, failureMsg))
        }
    }

    private var newImageUriHolder: Uri? = null

    fun setNewImageUri(imageUri: Uri?) {
        newImageUriHolder = imageUri
    }

    val selectedRecipe = MutableLiveData<Recipe?>()

    fun selectRecipe(recipe: Recipe?) {
        selectedRecipe.value = recipe
    }

    val sharedRecipe = selectedRecipe.distinctUntilChanged().switchMap {
        liveData {
            //delay(1000)
            emit(it)
        }
    }

    private val workManager = WorkManager.getInstance(application)

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
}

enum class Event {
    ADD, UPDATE, DELETE
}

enum class Result {
    LOADING, SUCCESS, FAILURE
}

data class EventResult(val event: Event, val result: Result, val recipe: Recipe, var failureMsg: FirebaseError = FirebaseError.NONE)

/* fun addRecipe(recipe: Recipe) = liveData(IO) {
        try {
            val result = recipeRepo.addRecipe(recipe)
            publishEventResult()
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

    val eventResultBroadcaster = eventResultReceiver.distinctUntilChanged().switchMap { eventResult ->
        liveData {
            emit(eventResult)
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
    }*/