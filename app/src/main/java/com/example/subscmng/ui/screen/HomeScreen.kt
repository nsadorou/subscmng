package com.example.subscmng.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.subscmng.data.entity.PaymentCycle
import com.example.subscmng.data.entity.Subscription
import com.example.subscmng.ui.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddEdit: (Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allSubscriptions by viewModel.allSubscriptions.collectAsState(initial = emptyList())
    val monthlySubscriptions by viewModel.monthlySubscriptions.collectAsState(initial = emptyList())
    val yearlySubscriptions by viewModel.yearlySubscriptions.collectAsState(initial = emptyList())
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("すべて", "月額", "年額")
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // サマリーカード
        SummaryCard(
            monthlyTotal = uiState.monthlyTotal,
            yearlyTotal = uiState.yearlyTotal,
            modifier = Modifier.padding(16.dp)
        )
        
        // タブ
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // サブスクリプション一覧
        val subscriptions = when (selectedTab) {
            0 -> allSubscriptions
            1 -> monthlySubscriptions
            2 -> yearlySubscriptions
            else -> allSubscriptions
        }
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(subscriptions) { subscription ->
                SubscriptionCard(
                    subscription = subscription,
                    onEdit = { onNavigateToAddEdit(subscription.id) },
                    onDelete = { viewModel.deleteSubscription(subscription) },
                    onClick = { onNavigateToDetail(subscription.id) }
                )
            }
        }
        
        // FAB
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { onNavigateToAddEdit(0L) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "追加")
            }
        }
    }
}

@Composable
private fun SummaryCard(
    monthlyTotal: Double,
    yearlyTotal: Double,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.JAPAN)
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "支出サマリー",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "月額合計",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥${numberFormat.format(monthlyTotal)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "年額合計",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥${numberFormat.format(yearlyTotal)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionCard(
    subscription: Subscription,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.JAPAN)
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "¥${numberFormat.format(subscription.amount)} (${subscription.paymentCycle.displayName})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    subscription.expirationDate?.let { date ->
                        Text(
                            text = "期限: ${dateFormat.format(date)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Add, contentDescription = "編集")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "削除")
                    }
                }
            }
            
            if (subscription.memo.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subscription.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
