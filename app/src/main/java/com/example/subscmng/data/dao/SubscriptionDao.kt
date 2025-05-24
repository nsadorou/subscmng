package com.example.subscmng.data.dao

import androidx.room.*
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SubscriptionDao {
    
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY serviceName ASC")
    fun getAllActiveSubscriptions(): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Long): Subscription?
    
    @Query("SELECT * FROM subscriptions WHERE paymentCycle = :cycle AND isActive = 1")
    fun getSubscriptionsByCycle(cycle: PaymentCycle): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions WHERE expirationDate BETWEEN :startDate AND :endDate AND isActive = 1")
    suspend fun getSubscriptionsExpiringBetween(startDate: Date, endDate: Date): List<Subscription>
    
    @Query("SELECT SUM(amount) FROM subscriptions WHERE paymentCycle = :cycle AND isActive = 1")
    suspend fun getTotalAmountByCycle(cycle: PaymentCycle): Double?
    
    @Insert
    suspend fun insertSubscription(subscription: Subscription): Long
    
    @Update
    suspend fun updateSubscription(subscription: Subscription)
    
    @Delete
    suspend fun deleteSubscription(subscription: Subscription)
    
    @Query("UPDATE subscriptions SET isActive = 0 WHERE id = :id")
    suspend fun deactivateSubscription(id: Long)
}
