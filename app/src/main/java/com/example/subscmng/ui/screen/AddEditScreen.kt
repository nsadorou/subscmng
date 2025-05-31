package com.example.subscmng.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.subscmng.data.entity.Currency
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.ui.viewmodel.AddEditViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    
    LaunchedEffect(subscriptionId) {
        viewModel.loadSubscription(subscriptionId)
    }
    
    // エラーメッセージの表示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Snackbar表示のロジックをここに追加
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // トップバー
        TopAppBar(
            title = { Text(if (uiState.isEditMode) "編集" else "追加") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                }
            },
            actions = {
                TextButton(
                    onClick = { viewModel.saveSubscription(onNavigateBack) }
                ) {
                    Text("保存")
                }
            }
        )
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // サービス名
            OutlinedTextField(
                value = uiState.serviceName,
                onValueChange = viewModel::updateServiceName,
                label = { Text("サービス名") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 金額
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("金額") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            // 通貨
            Text(
                text = "通貨",
                style = MaterialTheme.typography.titleMedium
            )
            
            Currency.values().forEach { currency ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.currency == currency,
                            onClick = { viewModel.updateCurrency(currency) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.currency == currency,
                        onClick = { viewModel.updateCurrency(currency) }
                    )
                    Text(
                        text = currency.displayName,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // 支払いサイクル
            Text(
                text = "支払いサイクル",
                style = MaterialTheme.typography.titleMedium
            )
            
            PaymentCycle.values().forEach { cycle ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.paymentCycle == cycle,
                            onClick = { viewModel.updatePaymentCycle(cycle) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.paymentCycle == cycle,
                        onClick = { viewModel.updatePaymentCycle(cycle) }
                    )
                    Text(
                        text = cycle.displayName,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // 支払日
            OutlinedTextField(
                value = uiState.paymentDay.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { day ->
                        if (day in 1..31) {
                            viewModel.updatePaymentDay(day)
                        }
                    }
                },
                label = { Text("支払日") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            // 有効期限（簡略化版）
            OutlinedTextField(
                value = uiState.expirationDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = { },
                label = { Text("有効期限（オプション）") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        // 簡易的な日付設定（現在日時から30日後）
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_MONTH, 30)
                        viewModel.updateExpirationDate(calendar.time)
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "30日後に設定")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // メモ
            OutlinedTextField(
                value = uiState.memo,
                onValueChange = viewModel::updateMemo,
                label = { Text("メモ") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
