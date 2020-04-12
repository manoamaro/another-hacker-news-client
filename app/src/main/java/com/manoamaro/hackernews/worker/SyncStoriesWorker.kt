package com.manoamaro.hackernews.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.manoamaro.hackernews.api.HackerNewsApi
import com.manoamaro.hackernews.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class SyncStoriesWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), KoinComponent {
    private val api: HackerNewsApi by inject()
    private val db: AppDatabase by inject()

    private suspend fun fetchAndUpdateItem(itemId: Int) = withContext(Dispatchers.IO) {
        try {
            db.itemDao().getItem(itemId) ?: api.getItem(itemId)?.let { updatedItem ->
                db.itemDao().insert(updatedItem)
            }
        } catch (e: java.lang.Exception) {
            Log.w(UpdateStoriesWorker.TASK_ID, "Could not update item=$itemId", e)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val selectedNewsChannel = NewsChannel.valueOf(this@SyncStoriesWorker.inputData.getString("channel") ?: NewsChannel.TOP_STORIES.name)
            when (selectedNewsChannel) {
                NewsChannel.TOP_STORIES -> api.getTopStoriesIds()
                NewsChannel.NEWEST_STORIES -> api.getNewestStoriesIds()
                NewsChannel.BEST_STORIES -> api.getBestStoriesIds()
            }.map { id -> this.async { fetchAndUpdateItem(id) } }.awaitAll()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        val TASK_ID = "SyncStoriesWorker"

        enum class NewsChannel {
            TOP_STORIES,
            NEWEST_STORIES,
            BEST_STORIES
        }

        fun startUniqueWork(workManager: WorkManager, channel: NewsChannel) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val data = Data.Builder()
                .putString("channel", channel.name)
                .build()

            val syncItems = OneTimeWorkRequestBuilder<SyncStoriesWorker>()
                .setInputData(data)
                .setConstraints(constraints).build()
            workManager.beginUniqueWork(TASK_ID, ExistingWorkPolicy.KEEP, syncItems).enqueue()
        }
    }
}