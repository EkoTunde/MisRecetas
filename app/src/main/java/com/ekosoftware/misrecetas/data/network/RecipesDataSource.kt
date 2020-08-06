package com.ekosoftware.misrecetas.data.network

import com.ekosoftware.misrecetas.domain.model.Recipe
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    @ExperimentalCoroutinesApi
    suspend fun getRecipes(filter: String): Flow<Resource<List<Recipe>>> {

        return callbackFlow {

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
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    suspend fun addRecipe(recipe: Recipe): Resource<String> {
        recipesRef.add(recipe).await()
        recipe.name?.let { name ->
            return Resource.Success(name)
        }
        throw IllegalArgumentException("The recipe was successfully, but it hasn't got a name")
    }

    suspend fun updateRecipe(recipe: Recipe): Resource<String> {
        recipe.id?.let { docId ->
            recipesRef.document(docId).set(recipe).await()
            recipe.name?.let { name ->
                return Resource.Success(name)
            }
            throw IllegalArgumentException("The update was successful but the recipe hasn't got a name")
        }
        throw IllegalArgumentException("To update a recipe an id must be passed as a parameter")
    }

    suspend fun deleteRecipe(recipe: Recipe): Resource<String> {
        recipe.id?.let { id ->
            recipesRef.document(id).delete().await()
            recipe.name?.let { name ->
                return Resource.Success(name)
            }
            throw IllegalArgumentException("The update was successful but the recipe hasn't got a name")
        }
        throw IllegalArgumentException("To delete a recipe an id must be passed as a parameter")
    }
}