package com.ekosoftware.misrecetas.domain.model

import com.ekosoftware.misrecetas.domain.constants.Event
import com.ekosoftware.misrecetas.domain.constants.FirebaseError
import com.ekosoftware.misrecetas.domain.constants.Result

data class EventResult(
    val event: Event,
    val result: Result,
    val recipe: Recipe,
    var failureMsg: FirebaseError = FirebaseError.NONE
)