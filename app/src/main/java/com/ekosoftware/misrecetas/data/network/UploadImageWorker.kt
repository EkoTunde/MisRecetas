package com.ekosoftware.misrecetas.data.network

import android.content.Context
import android.net.Uri
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
    }

    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result {
        return try {

            // Get image uri stored in cache
            val resourceUri = inputData.getString(KEY_IMAGE_URI)

            // Get the UUID name for the image (used in case download url isn't save to Recipe)
            val name = inputData.getString(KEY_IMAGE_NAME)

            // Progress 0 an 100 - Future feature
            val firstUpdate = workDataOf(Progress to 0)
            val lastUpdate = workDataOf(Progress to 100)

            // Publish 0 - Future feature
            setProgress(firstUpdate)

            val ref = FirebaseStorage.getInstance().reference.child("${RecipesDataSource.IMAGES_BUCKET}$name.jpg")
            val uploadTask = ref.putFile(Uri.parse(resourceUri))

            uploadTask.addOnProgressListener {
                if (this@UploadImageWorker.isStopped) {
                    uploadTask.cancel()
                    Result.failure()
                }

                // Get current progress - Future feature
                val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                // Publish current progress - Future feature
                setProgressAsync(workDataOf(Progress to progress))
            }

            // Gets download url to inform ViewModel
            val downloadUri = uploadTask
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            // retry policy if operation has failed - handle by the system
                            Result.retry()
                            throw it
                        }
                    }
                    ref.downloadUrl
                }.await()

            // Output containing the original uri (of image in cache) and the download url
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