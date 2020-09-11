package com.ekosoftware.misrecetas.data.network

object UploadingImage {

    private const val UPLOADING = 1
    const val STOPPED = 2

    private var state = STOPPED

    val currentState get() = state

    fun cancel() {
        state = STOPPED
    }

    fun start() {
        state = UPLOADING
    }

}