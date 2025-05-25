package com.example.subscmng.notification

import android.app.NotificationManager
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import com.example.subscmng.data.repository.SubscriptionRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class NotificationWorkerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var workerParams: WorkerParameters

    @Mock
    private lateinit var notificationManager: NotificationManager

    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var notificationWorker: NotificationWorker

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // MockKを使用してRepositoryをモック化
        subscriptionRepository = mockk()
        
        // Contextのモック設定
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { notificationManager.createNotificationChannel(any()) } just Runs
        every { notificationManager.notify(any<Int>(), any()) } just Runs
        
        // NotificationWorkerのインスタンスを作成
        notificationWorker = NotificationWorker(context, workerParams, subscriptionRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `doWork returns success when no exceptions occur`() = runTest {
        // Given
        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val oneWeekLater = calendar.time
        
        coEvery { 
            subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()) 
        } returns emptyList()

        // When
        val result = notificationWorker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()) }
    }

    @Test
    fun `doWork returns failure when exception occurs`() = runTest {
        // Given
        coEvery { 
            subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()) 
        } throws RuntimeException("Database error")

        // When
        val result = notificationWorker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `checkExpiringSubscriptions calls repository with correct date range`() = runTest {
        // Given
        val testSubscriptions = listOf(
            createTestSubscription(1, "Netflix"),
            createTestSubscription(2, "Spotify")
        )
        
        coEvery { 
            subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()) 
        } returns testSubscriptions

        // When
        notificationWorker.doWork()

        // Then
        coVerify { 
            subscriptionRepository.getSubscriptionsExpiringBetween(
                match { date -> 
                    // 今日の日付であることを確認（時間の差は1分以内）
                    Math.abs(date.time - Date().time) < 60000
                },
                match { date ->
                    // 7日後の日付であることを確認（時間の差は1分以内）
                    val expectedDate = Calendar.getInstance().apply { 
                        add(Calendar.DAY_OF_MONTH, 7) 
                    }.time
                    Math.abs(date.time - expectedDate.time) < 60000
                }
            )
        }
    }

    @Test
    fun `showNotification is called for each expiring subscription`() = runTest {
        // Given
        val testSubscriptions = listOf(
            createTestSubscription(1, "Netflix"),
            createTestSubscription(2, "Spotify"),
            createTestSubscription(3, "YouTube Premium")
        )
        
        coEvery { 
            subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()) 
        } returns testSubscriptions

        // When
        notificationWorker.doWork()

        // Then
        verify(exactly = testSubscriptions.size) { 
            notificationManager.notify(any<Int>(), any()) 
        }
    }

    @Test
    fun `notification is created with correct content`() = runTest {
        // Given
        val testSubscription = createTestSubscription(1, "Netflix")
        
        coEvery { 
            subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()) 
        } returns listOf(testSubscription)

        // When
        notificationWorker.doWork()

        // Then
        verify { 
            notificationManager.notify(
                eq(testSubscription.serviceName.hashCode()), 
                any()
            ) 
        }
    }

    @Test
    fun `scheduleNotificationCheck creates periodic work request`() {
        // Given
        val mockContext = mockk<Context>()
        val mockWorkManager = mockk<androidx.work.WorkManager>()
        
        every { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()
        mockkStatic(androidx.work.WorkManager::class)
        every { androidx.work.WorkManager.getInstance(mockContext) } returns mockWorkManager

        // When
        NotificationWorker.scheduleNotificationCheck(mockContext)

        // Then
        verify { 
            mockWorkManager.enqueueUniquePeriodicWork(
                "subscription_notification_check",
                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                any()
            ) 
        }
    }

    @Test
    fun `empty subscription list does not trigger notifications`() = runTest {
        // Given
        coEvery { 
            subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()) 
        } returns emptyList()

        // When
        val result = notificationWorker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notificationManager.notify(any<Int>(), any()) }
    }

    private fun createTestSubscription(
        id: Long,
        serviceName: String,
        expirationDate: Date? = Date()
    ): Subscription {
        return Subscription(
            id = id,
            serviceName = serviceName,
            amount = 980.0,
            currency = "JPY",
            paymentCycle = PaymentCycle.MONTHLY,
            paymentDay = 1,
            expirationDate = expirationDate,
            memo = "Test subscription",
            isActive = true,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}
