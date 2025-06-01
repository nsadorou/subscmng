package com.example.subscmng.data.repository

import com.example.subscmng.data.dao.SubscriptionDao
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
class SubscriptionRepositoryTest {

    @Mock
    private lateinit var subscriptionDao: SubscriptionDao

    private lateinit var subscriptionRepository: SubscriptionRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        subscriptionRepository = SubscriptionRepository(subscriptionDao)
    }

    @Test
    fun `getAllActiveSubscriptions returns flow from dao`() = runTest {
        // Given
        val testSubscriptions = listOf(
            createTestSubscription(1, "Netflix"),
            createTestSubscription(2, "Spotify")
        )
        whenever(subscriptionDao.getAllActiveSubscriptions())
            .thenReturn(flowOf(testSubscriptions))

        // When
        val result = subscriptionRepository.getAllActiveSubscriptions().first()

        // Then
        assertEquals(testSubscriptions, result)
        verify(subscriptionDao).getAllActiveSubscriptions()
    }

    @Test
    fun `getSubscriptionById returns subscription from dao`() = runTest {
        // Given
        val testSubscription = createTestSubscription(1, "Netflix")
        whenever(subscriptionDao.getSubscriptionById(1))
            .thenReturn(testSubscription)

        // When
        val result = subscriptionRepository.getSubscriptionById(1)

        // Then
        assertEquals(testSubscription, result)
        verify(subscriptionDao).getSubscriptionById(1)
    }

    @Test
    fun `getSubscriptionById returns null when not found`() = runTest {
        // Given
        whenever(subscriptionDao.getSubscriptionById(999))
            .thenReturn(null)

        // When
        val result = subscriptionRepository.getSubscriptionById(999)

        // Then
        assertNull(result)
        verify(subscriptionDao).getSubscriptionById(999)
    }

    @Test
    fun `getSubscriptionsByCycle returns flow from dao for monthly cycle`() = runTest {
        // Given
        val monthlySubscriptions = listOf(
            createTestSubscription(1, "Netflix", paymentCycle = PaymentCycle.MONTHLY),
            createTestSubscription(2, "Spotify", paymentCycle = PaymentCycle.MONTHLY)
        )
        whenever(subscriptionDao.getSubscriptionsByCycle(PaymentCycle.MONTHLY))
            .thenReturn(flowOf(monthlySubscriptions))

        // When
        val result = subscriptionRepository.getSubscriptionsByCycle(PaymentCycle.MONTHLY).first()

        // Then
        assertEquals(monthlySubscriptions, result)
        verify(subscriptionDao).getSubscriptionsByCycle(PaymentCycle.MONTHLY)
    }

    @Test
    fun `getSubscriptionsByCycle returns flow from dao for yearly cycle`() = runTest {
        // Given
        val yearlySubscriptions = listOf(
            createTestSubscription(3, "Adobe Creative Cloud", paymentCycle = PaymentCycle.YEARLY)
        )
        whenever(subscriptionDao.getSubscriptionsByCycle(PaymentCycle.YEARLY))
            .thenReturn(flowOf(yearlySubscriptions))

        // When
        val result = subscriptionRepository.getSubscriptionsByCycle(PaymentCycle.YEARLY).first()

        // Then
        assertEquals(yearlySubscriptions, result)
        verify(subscriptionDao).getSubscriptionsByCycle(PaymentCycle.YEARLY)
    }

    @Test
    fun `getSubscriptionsExpiringBetween returns list from dao`() = runTest {
        // Given
        val startDate = Date()
        val endDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }.time
        val expiringSubscriptions = listOf(
            createTestSubscription(1, "Netflix"),
            createTestSubscription(2, "Spotify")
        )
        whenever(subscriptionDao.getSubscriptionsExpiringBetween(startDate, endDate))
            .thenReturn(expiringSubscriptions)

        // When
        val result = subscriptionRepository.getSubscriptionsExpiringBetween(startDate, endDate)

        // Then
        assertEquals(expiringSubscriptions, result)
        verify(subscriptionDao).getSubscriptionsExpiringBetween(startDate, endDate)
    }

    @Test
    fun `getTotalAmountByCycle returns sum for monthly subscriptions`() = runTest {
        // Given
        val totalAmount = 2960.0
        whenever(subscriptionDao.getTotalAmountByCycle(PaymentCycle.MONTHLY))
            .thenReturn(totalAmount)

        // When
        val result = subscriptionRepository.getTotalAmountByCycle(PaymentCycle.MONTHLY)

        // Then
        assertEquals(totalAmount, result, 0.01)
        verify(subscriptionDao).getTotalAmountByCycle(PaymentCycle.MONTHLY)
    }

    @Test
    fun `getTotalAmountByCycle returns 0 when dao returns null`() = runTest {
        // Given
        whenever(subscriptionDao.getTotalAmountByCycle(PaymentCycle.YEARLY))
            .thenReturn(null)

        // When
        val result = subscriptionRepository.getTotalAmountByCycle(PaymentCycle.YEARLY)

        // Then
        assertEquals(0.0, result, 0.01)
        verify(subscriptionDao).getTotalAmountByCycle(PaymentCycle.YEARLY)
    }

    @Test
    fun `insertSubscription returns id from dao`() = runTest {
        // Given
        val newSubscription = createTestSubscription(0, "Disney+")
        val generatedId = 5L
        whenever(subscriptionDao.insertSubscription(newSubscription))
            .thenReturn(generatedId)

        // When
        val result = subscriptionRepository.insertSubscription(newSubscription)

        // Then
        assertEquals(generatedId, result)
        verify(subscriptionDao).insertSubscription(newSubscription)
    }

    @Test
    fun `updateSubscription calls dao update method`() = runTest {
        // Given
        val subscription = createTestSubscription(1, "Netflix Updated")

        // When
        subscriptionRepository.updateSubscription(subscription)

        // Then
        verify(subscriptionDao).updateSubscription(subscription)
    }

    @Test
    fun `deleteSubscription calls dao delete method`() = runTest {
        // Given
        val subscription = createTestSubscription(1, "Netflix")

        // When
        subscriptionRepository.deleteSubscription(subscription)

        // Then
        verify(subscriptionDao).deleteSubscription(subscription)
    }

    @Test
    fun `deactivateSubscription calls dao deactivate method`() = runTest {
        // Given
        val subscriptionId = 1L

        // When
        subscriptionRepository.deactivateSubscription(subscriptionId)

        // Then
        verify(subscriptionDao).deactivateSubscription(subscriptionId)
    }

    @Test
    fun `repository methods handle dao exceptions correctly`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        whenever(subscriptionDao.getSubscriptionById(1))
            .thenThrow(exception)

        // When/Then
        try {
            subscriptionRepository.getSubscriptionById(1)
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }

    private fun createTestSubscription(
        id: Long,
        serviceName: String,
        paymentCycle: PaymentCycle = PaymentCycle.MONTHLY,
        amount: Double = 980.0,
        expirationDate: Date? = Date()
    ): Subscription {
        return Subscription(
            id = id,
            serviceName = serviceName,
            amount = amount,
            currency = "JPY",
            paymentCycle = paymentCycle,
            paymentDay = 1,
            expirationDate = expirationDate,
            memo = "Test subscription",
            isActive = true,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}