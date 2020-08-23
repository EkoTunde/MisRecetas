package com.ekosoftware.misrecetas.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await

class UploadImageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val Progress = "Progress"
        const val KEY_IMAGE_URI = "image uri key"
        const val KEY_OUTPUT_DOWNLOAD_IMAGE_URI = "download uri"
        const val KEY_IMAGE_NAME = "image name"
        const val EXCEPTION_OPERATION_CANCELED_BY_USER = "Operation canceled by user"
        private const val TAG = "UploadImageWorker"
    }

    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result {
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val name = inputData.getString(KEY_IMAGE_NAME)
            Log.d(TAG, "doWork: work started for $name")
            val firstUpdate = workDataOf(Progress to 0)
            val lastUpdate = workDataOf(Progress to 100)
            setProgress(firstUpdate)

            /*uploadImage(resourceUri, name).collect { workerResource ->
                when (workerResource) {
                    is WorkerResource.Loading -> {
                        setProgress(workDataOf(Progress to workerResource.progress))
                        Result.failure()
                    }
                    is WorkerResource.Success -> {
                        val outputData = workerResource.data
                        setProgress(lastUpdate)
                        Log.d(TAG, "doWork: SUCCESS!")
                        Result.success(outputData)
                    }
                    is WorkerResource.Failure -> {
                        Result.failure()
                        throw Exception(workerResource.exception)
                    }
                }
            }
            Result.failure()*/
            val ref = FirebaseStorage.getInstance().reference.child("hello/$name.jpg")
            val uploadTask = ref.putFile(Uri.parse(resourceUri))

            uploadTask.addOnProgressListener {
                if (this@UploadImageWorker.isStopped) {
                    uploadTask.cancel()
                    Result.failure()
                }
                val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                setProgressAsync(workDataOf(Progress to progress))
            }

            val downloadUri = uploadTask
                .continueWithTask { task ->
                    //Log.d(TAG, "doWork: attempting to start")
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            Result.retry()
                            throw it
                        }
                    }
                    ref.downloadUrl
                }.await()
            val outputData = workDataOf(
                KEY_IMAGE_URI to resourceUri,
                KEY_OUTPUT_DOWNLOAD_IMAGE_URI to downloadUri.toString()
            )
            setProgress(lastUpdate)
            Result.success(outputData)
        } catch (e: Exception) {
            Result.failure()
        }
    }
}