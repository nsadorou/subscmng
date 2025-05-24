package com.example.subscmng.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceName: String,
    val amount: Double,
    val currency: String = "JPY",
    val paymentCycle: PaymentCycle,
    val paymentDay: Int, // 1-31 for monthly, 1-365 for yearly
    val expirationDate: Date?,
    val memo: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class PaymentCycle(val displayName: String) {
    MONTHLY("月額"),
    YEARLY("年額")
}
