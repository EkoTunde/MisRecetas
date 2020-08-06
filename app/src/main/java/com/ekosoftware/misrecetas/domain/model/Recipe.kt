package com.ekosoftware.misrecetas.domain.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recipe(
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var timeRequired: Long? = null,
    var servings: Long? = null,
    var ingredients: List<String>? = null,
    var instructions: List<String>? = null,
    var creationDate: Timestamp? = null,
    var creator: User? = null,
    var isFavorite: Boolean? = null,
    var keywords: List<String>? = null
) : Parcelable