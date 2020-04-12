package com.manoamaro.hackernews.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.manoamaro.hackernews.R
import com.manoamaro.hackernews.api.HackerNewsApi
import com.manoamaro.hackernews.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdateStoriesWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), KoinComponent {
    private val api: HackerNewsApi by inject()
    private val db: AppDatabase by inject()
    private val notificationManager: NotificationManagerCompat by inject()
    private val workManager: WorkManager by inject()

    private suspend fun fetchAndUpdateItem(itemId: Int) = withContext(Dispatchers.IO) {
        try {
            api.getItem(itemId)?.let { updatedItem ->
                if (db.itemDao().insert(updatedItem) <= 0) {
                    db.itemDao().updateItems(updatedItem)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.w(TASK_ID, "Could not update item=$itemId", e)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setForeground(createForegroundInfo())
            val updatedItems = api.getUpdates().items
            updatedItems.map{ itemId -> async { fetchAndUpdateItem(itemId) } }.awaitAll()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val id = applicationContext.getString(R.string.notification_channel_id)
        val title = applicationContext.getString(R.string.notification_update_items_title)
        val cancel = applicationContext.getString(R.string.notification_cancel)

        val cancelIntent = workManager.createCancelPendingIntent(getId())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    id,
                    title,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setProgress(100, 0, true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(android.R.drawable.ic_delete, cancel, cancelIntent)
            .build()
        return ForegroundInfo(1, notification)
    }

    companion object {
        val TASK_ID = "UpdateStoriesWorker"
    }
}