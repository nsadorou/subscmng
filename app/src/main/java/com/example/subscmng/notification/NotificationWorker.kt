package com.example.subscmng.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.subscmng.R
import com.example.subscmng.data.repository.SubscriptionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val subscriptionRepository: SubscriptionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            checkExpiringSubscriptions()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun checkExpiringSubscriptions() {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // 7日後までに期限が切れるサブスクリプションをチェック
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val oneWeekLater = calendar.time
        
        val expiringSubscriptions = subscriptionRepository.getSubscriptionsExpiringBetween(today, oneWeekLater)
        
        expiringSubscriptions.forEach { subscription ->
            showNotification(subscription.serviceName, subscription.expirationDate)
        }
    }

    private fun showNotification(serviceName: String, expirationDate: Date?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("サブスク期限通知")
            .setContentText("$serviceName の期限が近づいています")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(serviceName.hashCode(), notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "サブスク通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "サブスクリプションの期限通知"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "subscription_notification_channel"
        
        fun scheduleNotificationCheck(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
                
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "subscription_notification_check",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
