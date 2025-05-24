package com.example.subscmng.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscmng.data.entity.Subscription
import com.example.subscmng.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : androidx.lifecycle.ViewModel() {
    
    private val _subscription = mutableStateOf<Subscription?>(null)
    val subscription: State<Subscription?> = _subscription
    
    fun loadSubscription(id: Long) {
        viewModelScope.launch {
            _subscription.value = subscriptionRepository.getSubscriptionById(id)
        }
    }
    
    fun deleteSubscription(subscription: Subscription, onDeleted: () -> Unit) {
        viewModelScope.launch {
            subscriptionRepository.deleteSubscription(subscription)
            onDeleted()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val subscription by viewModel.subscription
    val numberFormat = NumberFormat.getNumberInstance(Locale.JAPAN)
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    
    LaunchedEffect(subscriptionId) {
        viewModel.loadSubscription(subscriptionId)
    }
    
    subscription?.let { sub ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // トップバー
            TopAppBar(
                title = { Text(sub.serviceName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(sub.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "編集")
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteSubscription(sub, onNavigateBack)
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "削除")
                    }
                }
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 基本情報カード
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "基本情報",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        DetailRow("サービス名", sub.serviceName)
                        DetailRow("金額", "¥${numberFormat.format(sub.amount)}")
                        DetailRow("支払いサイクル", sub.paymentCycle.displayName)
                        DetailRow("支払日", "${sub.paymentDay}日")
                        
                        sub.expirationDate?.let { date ->
                            DetailRow("有効期限", dateFormat.format(date))
                        }
                    }
                }
                
                // メモカード
                if (sub.memo.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "メモ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sub.memo,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // 作成・更新日時カード
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "日時情報",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        DetailRow("作成日時", dateFormat.format(sub.createdAt))
                        DetailRow("更新日時", dateFormat.format(sub.updatedAt))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
