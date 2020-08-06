package com.ekosoftware.misrecetas.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var uid: String? = null,
    var displayName: String? = null,
    var phoneNumber: String? = null,
    var email: String? = null,
    var imageUrl: String? = null
) : Parcelable