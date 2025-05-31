package com.example.subscmng.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscmng.data.entity.Currency
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import com.example.subscmng.data.repository.SubscriptionRepository
import com.example.subscmng.data.service.ExchangeRateService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val exchangeRateService: ExchangeRateService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()
    
    fun loadSubscription(id: Long) {
        if (id == 0L) return
        
        viewModelScope.launch {
            val subscription = subscriptionRepository.getSubscriptionById(id)
            subscription?.let {
                _uiState.value = _uiState.value.copy(
                    id = it.id,
                    serviceName = it.serviceName,
                    amount = it.amount.toString(),
                    currency = Currency.values().find { curr -> curr.code == it.currency } ?: Currency.JPY,
                    paymentCycle = it.paymentCycle,
                    paymentDay = it.paymentDay,
                    expirationDate = it.expirationDate,
                    memo = it.memo,
                    isEditMode = true
                )
            }
        }
    }
    
    fun updateServiceName(serviceName: String) {
        _uiState.value = _uiState.value.copy(serviceName = serviceName)
    }
    
    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }
    
    fun updateCurrency(currency: Currency) {
        _uiState.value = _uiState.value.copy(currency = currency)
    }
    
    fun updatePaymentCycle(cycle: PaymentCycle) {
        _uiState.value = _uiState.value.copy(paymentCycle = cycle)
    }
    
    fun updatePaymentDay(day: Int) {
        _uiState.value = _uiState.value.copy(paymentDay = day)
    }
    
    fun updateExpirationDate(date: Date?) {
        _uiState.value = _uiState.value.copy(expirationDate = date)
    }
    
    fun updateMemo(memo: String) {
        _uiState.value = _uiState.value.copy(memo = memo)
    }
    
    fun saveSubscription(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        if (state.serviceName.isBlank() || state.amount.isBlank()) {
            _uiState.value = state.copy(errorMessage = "サービス名と金額は必須です")
            return
        }
        
        val inputAmount = state.amount.toDoubleOrNull()
        if (inputAmount == null || inputAmount <= 0) {
            _uiState.value = state.copy(errorMessage = "正しい金額を入力してください")
            return
        }
        
        viewModelScope.launch {
            try {
                // Convert USD to JPY if needed
                val (finalAmount, finalCurrency) = if (state.currency == Currency.USD) {
                    val exchangeResult = exchangeRateService.getUsdToJpyRate()
                    exchangeResult.fold(
                        onSuccess = { rate ->
                            val jpyAmount = exchangeRateService.convertUsdToJpy(inputAmount, rate)
                            Pair(jpyAmount, "JPY")
                        },
                        onFailure = {
                            _uiState.value = state.copy(errorMessage = "為替レートの取得に失敗しました")
                            return@launch
                        }
                    )
                } else {
                    Pair(inputAmount, state.currency.code)
                }
                
                val subscription = Subscription(
                    id = state.id,
                    serviceName = state.serviceName,
                    amount = finalAmount,
                    currency = finalCurrency,
                    paymentCycle = state.paymentCycle,
                    paymentDay = state.paymentDay,
                    expirationDate = state.expirationDate,
                    memo = state.memo,
                    updatedAt = Date()
                )
                
                if (state.isEditMode) {
                    subscriptionRepository.updateSubscription(subscription)
                } else {
                    subscriptionRepository.insertSubscription(subscription)
                }
                
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = state.copy(errorMessage = "保存に失敗しました")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AddEditUiState(
    val id: Long = 0L,
    val serviceName: String = "",
    val amount: String = "",
    val currency: Currency = Currency.JPY,
    val paymentCycle: PaymentCycle = PaymentCycle.MONTHLY,
    val paymentDay: Int = 1,
    val expirationDate: Date? = null,
    val memo: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
