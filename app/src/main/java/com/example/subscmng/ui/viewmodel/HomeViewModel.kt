package com.example.subscmng.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import com.example.subscmng.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    val allSubscriptions = subscriptionRepository.getAllActiveSubscriptions()
    val monthlySubscriptions = subscriptionRepository.getSubscriptionsByCycle(PaymentCycle.MONTHLY)
    val yearlySubscriptions = subscriptionRepository.getSubscriptionsByCycle(PaymentCycle.YEARLY)
    
    init {
        loadTotals()
    }
    
    private fun loadTotals() {
        viewModelScope.launch {
            val monthlyTotal = subscriptionRepository.getTotalAmountByCycle(PaymentCycle.MONTHLY)
            val yearlyTotal = subscriptionRepository.getTotalAmountByCycle(PaymentCycle.YEARLY)
            
            _uiState.value = _uiState.value.copy(
                monthlyTotal = monthlyTotal,
                yearlyTotal = yearlyTotal,
                isLoading = false
            )
        }
    }
    
    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            subscriptionRepository.deleteSubscription(subscription)
            loadTotals()
        }
    }
}

data class HomeUiState(
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val isLoading: Boolean = true
)
