package com.ekosoftware.misrecetas.data.network

import com.ekosoftware.misrecetas.domain.model.CurrentUser
import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecipesDataSource {

    companion object {
        const val IMAGES_BUCKET = "recipes/images/"
    }

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val recipesRef by lazy { db.collection("recipes") }
    private val bucket by lazy { FirebaseStorage.getInstance().reference }

    @ExperimentalCoroutinesApi
    suspend fun getAllRecipes(): Flow<Resource<List<Recipe>>> = callbackFlow {
        val listenerRegistration = recipesRef.whereEqualTo("creator", CurrentUser.data.uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    cancel(
                        message = it.message
                            ?: "error fetching documents at ${System.currentTimeMillis()}",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                offer(
                    Resource.Success(
                        querySnapshot?.map { queryDocumentSnapshot ->
                            queryDocumentSnapshot.toObject(Recipe::class.java).apply {
                                id = queryDocumentSnapshot.id
                            }
                        } ?: listOf())
                )
                return@addSnapshotListener
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getRecipes(filter: String): Flow<Resource<List<Recipe>>> = callbackFlow {
        val listenerRegistration = recipesRef.whereArrayContains("keywords", filter)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    cancel(
                        message = it.message
                            ?: "error fetching documents at ${System.currentTimeMillis()}",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                offer(
                    Resource.Success(
                        querySnapshot?.map { queryDocumentSnapshot ->
                            queryDocumentSnapshot.toObject(Recipe::class.java).apply {
                                id = queryDocumentSnapshot.id
                            }
                        } ?: listOf())
                )
                return@addSnapshotListener
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun addRecipe(recipe: Recipe): String {
        recipe.apply {
            creator = CurrentUser.data.uid
            creationDate = Timestamp.now()
        }.also {
            val docRef = recipesRef.add(it).await()
            return recipe.name!!
        }
    }

    suspend fun updateRecipe(recipe: Recipe): String {
        recipe.id?.let { docId ->
            recipesRef.document(docId).set(recipe).await()
            return recipe.name!!
        }
        throw IllegalArgumentException("Recipe id was null when trying to update")
    }

    suspend fun deleteRecipe(recipe: Recipe): String {
        recipe.id?.let { id ->
            recipesRef.document(id).delete().await()
            return recipe.name!!
        }
        throw IllegalArgumentException("Recipe id was null when trying to delete")
    }

    suspend fun deleteImage(recipeId: String?, uuid: String) {
        bucket.child("$IMAGES_BUCKET$uuid.jpg").delete().await()
        recipeId?.let {
            recipesRef.document(it).update("imageUrl", "").await()
        }
    }
}