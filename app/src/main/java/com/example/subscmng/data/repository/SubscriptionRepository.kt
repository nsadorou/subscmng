package com.example.subscmng.data.repository

import com.example.subscmng.data.dao.SubscriptionDao
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    
    fun getAllActiveSubscriptions(): Flow<List<Subscription>> =
        subscriptionDao.getAllActiveSubscriptions()
    
    suspend fun getSubscriptionById(id: Long): Subscription? =
        subscriptionDao.getSubscriptionById(id)
    
    fun getSubscriptionsByCycle(cycle: PaymentCycle): Flow<List<Subscription>> =
        subscriptionDao.getSubscriptionsByCycle(cycle)
    
    suspend fun getSubscriptionsExpiringBetween(startDate: Date, endDate: Date): List<Subscription> =
        subscriptionDao.getSubscriptionsExpiringBetween(startDate, endDate)
    
    suspend fun getTotalAmountByCycle(cycle: PaymentCycle): Double =
        subscriptionDao.getTotalAmountByCycle(cycle) ?: 0.0
    
    suspend fun insertSubscription(subscription: Subscription): Long =
        subscriptionDao.insertSubscription(subscription)
    
    suspend fun updateSubscription(subscription: Subscription) =
        subscriptionDao.updateSubscription(subscription)
    
    suspend fun deleteSubscription(subscription: Subscription) =
        subscriptionDao.deleteSubscription(subscription)
    
    suspend fun deactivateSubscription(id: Long) =
        subscriptionDao.deactivateSubscription(id)
}
