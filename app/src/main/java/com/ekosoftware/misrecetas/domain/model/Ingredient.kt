package com.ekosoftware.misrecetas.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredient(val id: Int? = null, val name: String, var isChecked: Boolean = false) : Parcelable