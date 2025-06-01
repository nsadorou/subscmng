package com.example.subscmng.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

@Singleton
class ExchangeRateService @Inject constructor() {
    
    companion object {
        private const val CACHE_VALIDITY_MILLIS = 60 * 60 * 1000L // 1 hour
        private const val DEFAULT_FALLBACK_RATE = 150.0 // USD to JPY fallback rate
    }
    
    private val cachedRate = AtomicReference<Double?>(null)
    private val lastFetchTime = AtomicLong(0L)
    
    suspend fun getUsdToJpyRate(): Result<Double> = withContext(Dispatchers.IO) {
        try {
            // Use cached rate if it's still valid
            val currentTime = System.currentTimeMillis()
            cachedRate.get()?.let { rate ->
                if (currentTime - lastFetchTime.get() < CACHE_VALIDITY_MILLIS) {
                    return@withContext Result.success(rate)
                }
            }
            
            // Fetch new rate from free API
            val url = URL("https://api.exchangerate-api.com/v4/latest/USD")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(response)
                val rates = jsonObject.getJSONObject("rates")
                val jpyRate = rates.getDouble("JPY")
                
                // Update cache
                cachedRate.set(jpyRate)
                lastFetchTime.set(currentTime)
                
                Result.success(jpyRate)
            } else {
                // Fallback to cached rate if available, otherwise use default
                cachedRate.get()?.let { 
                    Result.success(it) 
                } ?: Result.success(DEFAULT_FALLBACK_RATE)
            }
        } catch (e: Exception) {
            // Return cached rate if available, otherwise use fallback
            cachedRate.get()?.let { 
                Result.success(it) 
            } ?: Result.success(DEFAULT_FALLBACK_RATE)
        }
    }
    
    fun convertUsdToJpy(usdAmount: Double, rate: Double): Double {
        return usdAmount * rate
    }
}