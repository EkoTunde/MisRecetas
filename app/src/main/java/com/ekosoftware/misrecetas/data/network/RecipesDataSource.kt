package com.ekosoftware.misrecetas.data.network

import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecipesDataSource {

    companion object {
        private val db = FirebaseFirestore.getInstance()
        private val recipesRef = db.collection("recipes")
        private val bucket = FirebaseStorage.getInstance().reference
        const val RECIPES_BUCKET = "recipes_images"
    }


    @ExperimentalCoroutinesApi
    suspend fun getAllRecipes(): Flow<Resource<List<Recipe>>> {

        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        return callbackFlow {

            val listenerRegistration = recipesRef.whereEqualTo("creator", uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    /*firebaseFirestoreException?.let {
                        cancel(
                            message = it.message
                                ?: "error fetching documents at ${System.currentTimeMillis()}",
                            cause = firebaseFirestoreException
                        )
                        return@addSnapshotListener
                    }*/
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
    }

    @ExperimentalCoroutinesApi
    suspend fun getRecipes(filter: String): Flow<Resource<List<Recipe>>> {

        return callbackFlow {

            val listenerRegistration = recipesRef.whereArrayContains("keywords", filter)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    /* firebaseFirestoreException?.let {
                         cancel(
                             message = it.message
                                 ?: "error fetching documents at ${System.currentTimeMillis()}",
                             cause = firebaseFirestoreException
                         )
                         return@addSnapshotListener
                     }*/
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
    }

    suspend fun addRecipe(recipe: Recipe): String {
        recipesRef.add(recipe).await()
        return recipe.name!!
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
}