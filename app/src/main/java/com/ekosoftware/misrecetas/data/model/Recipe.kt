package com.ekosoftware.misrecetas.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recipe(
    var id: String? = null,
    var name: String? = null, //
    var description: String? = null, //
    var difficulty: Difficulty? = null, //
    var imageUrl: String? = null,
    var timeRequired: String? = null, //
    var servings: Long? = null, //
    var ingredientes: List<Ingredient>? = null,
    var instructions: List<String>? = null,
    var creationDate: Timestamp? = null,
    var creator: User? = null, //
    var isFavorite: Boolean? = null,
    var rating: Double? = null
) : Parcelable

enum class Difficulty {
    EASY, NORMAL, HARD
}