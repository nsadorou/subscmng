package com.example.subscmng.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class ExchangeRateService @Inject constructor() {
    
    private var cachedRate: Double? = null
    private var lastFetchTime: Long = 0
    private val cacheValidityMillis = 60 * 60 * 1000 // 1 hour
    
    suspend fun getUsdToJpyRate(): Result<Double> = withContext(Dispatchers.IO) {
        try {
            // Use cached rate if it's still valid
            val currentTime = System.currentTimeMillis()
            cachedRate?.let { rate ->
                if (currentTime - lastFetchTime < cacheValidityMillis) {
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
                cachedRate = jpyRate
                lastFetchTime = currentTime
                
                Result.success(jpyRate)
            } else {
                // Fallback to cached rate if available, otherwise use default
                cachedRate?.let { 
                    Result.success(it) 
                } ?: Result.success(150.0) // Fallback rate
            }
        } catch (e: Exception) {
            // Return cached rate if available, otherwise use fallback
            cachedRate?.let { 
                Result.success(it) 
            } ?: Result.success(150.0) // Fallback rate when no network
        }
    }
    
    fun convertUsdToJpy(usdAmount: Double, rate: Double): Double {
        return usdAmount * rate
    }
}