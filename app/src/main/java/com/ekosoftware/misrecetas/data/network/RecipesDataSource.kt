package com.ekosoftware.misrecetas.data.network

import com.ekosoftware.misrecetas.data.model.Recipe
import com.ekosoftware.misrecetas.data.model.User
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecipesDataSource {

    companion object {
        private val db = FirebaseFirestore.getInstance()
        private val recipesRef = db.collection("recipes")
    }

    @ExperimentalCoroutinesApi
    suspend fun getAllRecipes(): Flow<Resource<List<Recipe>>> {

        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        return callbackFlow {

            val listenerRegistration = recipesRef.whereEqualTo("creator", uid)
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
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    suspend fun addRecipe(recipe: Recipe): Resource<Boolean> {
        val result = recipesRef.add(recipe).await()
        return Resource.Success(result.id.isNotEmpty())
    }

    suspend fun updateRecipe(recipe: Recipe): Resource<Boolean> {
        recipe.id?.let { docId ->
            recipesRef.document(docId).set(recipe).await()
            return Resource.Success(true)
        }
        return Resource.Success(false)
    }

    suspend fun deleteRecipe(docId: String): Resource<Boolean> {
        recipesRef.document(docId).delete().await()
        return Resource.Success(true)
    }
}