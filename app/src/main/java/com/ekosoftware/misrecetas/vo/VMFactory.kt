package com.ekosoftware.misrecetas.vo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.jetbrains.annotations.NotNull

class VMFactory<T: Any>(@NotNull vararg val parameterTypes:  T) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(parameterTypes.iterator().javaClass).newInstance(parameterTypes)
    }
}