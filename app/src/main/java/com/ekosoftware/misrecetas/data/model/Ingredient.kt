package com.ekosoftware.misrecetas.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredient(
    var name: String? = null,
    var amount: Long? = null,
    var unit: String? = null
) : Parcelable
