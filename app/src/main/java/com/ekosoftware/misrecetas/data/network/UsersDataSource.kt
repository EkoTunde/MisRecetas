package com.ekosoftware.misrecetas.data.network

import com.ekosoftware.misrecetas.data.model.CurrentUser
import com.ekosoftware.misrecetas.data.model.User
import com.ekosoftware.misrecetas.vo.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsersDataSource {

    companion object {
        const val NO_USER_FOUND = "No user was found for the provided id"
        private val db = FirebaseFirestore.getInstance()
        private val usersRef = db.collection("users")
        private val currentUser = FirebaseAuth.getInstance().currentUser
    }

    private val currentFirebaseUser by lazy {
        FirebaseAuth.getInstance().currentUser
    }

    private val isUserLoggedIn by lazy {
        updateCurrentUser(currentFirebaseUser)
        currentFirebaseUser != null
    }

    private fun updateCurrentUser(firebaseUser: FirebaseUser?) {
        firebaseUser?.let {
            CurrentUser.updateUser(
                User(
                    it.uid,
                    it.displayName,
                    it.phoneNumber,
                    it.email,
                    it.photoUrl.toString()
                )
            )
        }
    }

    fun checkIsUserLoggedIn() = isUserLoggedIn

    // Retrieves user data from firestore, with the user uid provided by FirebaseAuth
    suspend fun getUserData(): Resource<User> {
        val mdata = currentUser?.metadata
        val z = mdata?.creationTimestamp
        currentUser?.let {
            val result = usersRef.document(it.uid).get().await()
            val user = result.toObject(User::class.java) ?: throw Exception(NO_USER_FOUND)
            return Resource.Success(user)
        }
        return Resource.Failure(Exception("There wasn't any user logged in"))
    }

}