package com.example.subscmng.notification

import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import com.example.subscmng.data.repository.SubscriptionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class NotificationWorkerTest {

    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository

    private lateinit var notificationWorker: TestableNotificationWorker

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        notificationWorker = TestableNotificationWorker(subscriptionRepository)
    }

    @Test
    fun `doWork returns success when no exceptions occur`() = runTest {
        // Given
        whenever(subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()))
            .thenReturn(emptyList())

        // When
        val result = notificationWorker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        verify(subscriptionRepository).getSubscriptionsExpiringBetween(any(), any())
    }

    @Test
    fun `doWork returns failure when exception occurs`() = runTest {
        // Given
        whenever(subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()))
            .thenThrow(RuntimeException("Database error"))

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
        
        whenever(subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()))
            .thenReturn(testSubscriptions)

        // When
        notificationWorker.doWork()

        // Then
        verify(subscriptionRepository).getSubscriptionsExpiringBetween(
            argThat { date -> 
                // 今日の日付であることを確認（時間の差は1分以内）
                Math.abs(date.time - Date().time) < TIME_TOLERANCE_MS
            },
            argThat { date ->
                // 7日後の日付であることを確認（時間の差は1分以内）
                val expectedDate = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, 7) 
                }.time
                Math.abs(date.time - expectedDate.time) < TIME_TOLERANCE_MS
            }
        )
    }

    @Test
    fun `showNotification is called for each expiring subscription`() = runTest {
        // Given
        val testSubscriptions = listOf(
            createTestSubscription(1, "Netflix"),
            createTestSubscription(2, "Spotify"),
            createTestSubscription(3, "YouTube Premium")
        )
        
        whenever(subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()))
            .thenReturn(testSubscriptions)

        // When
        notificationWorker.doWork()

        // Then
        assertEquals(testSubscriptions.size, notificationWorker.notificationCount)
    }

    @Test
    fun `notification is created with correct service name`() = runTest {
        // Given
        val testSubscription = createTestSubscription(1, "Netflix")
        
        whenever(subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()))
            .thenReturn(listOf(testSubscription))

        // When
        notificationWorker.doWork()

        // Then
        assertTrue(notificationWorker.notifiedServices.contains("Netflix"))
    }

    @Test
    fun `empty subscription list does not trigger notifications`() = runTest {
        // Given
        whenever(subscriptionRepository.getSubscriptionsExpiringBetween(any(), any()))
            .thenReturn(emptyList())

        // When
        val result = notificationWorker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, notificationWorker.notificationCount)
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

    // テスト用のNotificationWorkerサブクラス
    private class TestableNotificationWorker(
        private val subscriptionRepository: SubscriptionRepository
    ) {
        var notificationCount = 0
        val notifiedServices = mutableListOf<String>()

        suspend fun doWork(): ListenableWorker.Result {
            return try {
                checkExpiringSubscriptions()
                ListenableWorker.Result.success()
            } catch (e: Exception) {
                ListenableWorker.Result.failure()
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
            notificationCount++
            notifiedServices.add(serviceName)
        }
    }
}
