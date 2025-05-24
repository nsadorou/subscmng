package com.example.subscmng

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.subscmng.notification.NotificationWorker
import com.example.subscmng.ui.screen.*
import com.example.subscmng.ui.theme.SubscMngTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 通知の設定を開始
            NotificationWorker.scheduleNotificationCheck(this)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 通知チャンネルの作成
        createNotificationChannel()
        
        // 通知権限の確認とリクエスト
        checkAndRequestNotificationPermission()
        
        setContent {
            SubscMngTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SubscriptionApp()
                }
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "subscription_notification_channel",
                "サブスク通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "サブスクリプションの期限通知"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 権限が既に付与されている場合
                    NotificationWorker.scheduleNotificationCheck(this)
                }
                else -> {
                    // 権限をリクエスト
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 13未満では権限不要
            NotificationWorker.scheduleNotificationCheck(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // ボトムナビゲーションを表示するかどうかの判定
    val showBottomBar = when (currentRoute) {
        "home", "settings" -> true
        else -> false
    }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToAddEdit = { id ->
                        navController.navigate("add_edit/$id")
                    },
                    onNavigateToDetail = { id ->
                        navController.navigate("detail/$id")
                    }
                )
            }
            
            composable("add_edit/{subscriptionId}") { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getString("subscriptionId")?.toLongOrNull() ?: 0L
                AddEditScreen(
                    subscriptionId = subscriptionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("detail/{subscriptionId}") { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getString("subscriptionId")?.toLongOrNull() ?: 0L
                DetailScreen(
                    subscriptionId = subscriptionId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate("add_edit/$id")
                    }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "ホーム") },
            label = { Text("ホーム") },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "設定") },
            label = { Text("設定") },
            selected = currentRoute == "settings",
            onClick = {
                navController.navigate("settings")
            }
        )
    }
}
