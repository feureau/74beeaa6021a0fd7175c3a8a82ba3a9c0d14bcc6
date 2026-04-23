package com.posapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posapp.data.local.AppDatabase
import com.posapp.data.local.MenuDataSource
import com.posapp.data.repository.MenuRepository
import com.posapp.data.repository.OrderRepository
import com.posapp.presentation.cart.CartScreen
import com.posapp.presentation.cart.CartViewModel
import com.posapp.presentation.cart.CartViewModelFactory
import com.posapp.presentation.history.HistoryScreen
import com.posapp.presentation.history.HistoryViewModel
import com.posapp.presentation.history.HistoryViewModelFactory
import com.posapp.presentation.menu.MenuScreen
import com.posapp.presentation.menu.MenuViewModel
import com.posapp.presentation.menu.MenuViewModelFactory
import com.posapp.presentation.printer.PrinterManager
import com.posapp.ui.theme.PosAppTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestBluetoothPermissions()

        val menuDataSource = MenuDataSource(this)
        val menuRepository = MenuRepository(menuDataSource)

        val database = AppDatabase.getDatabase(this)
        val orderRepository = OrderRepository(database.orderDao())
        val printerManager = PrinterManager(this)

        setContent {
            PosAppTheme {
                val menuViewModel: MenuViewModel = viewModel(
                    factory = MenuViewModelFactory(menuRepository)
                )

                val cartViewModel: CartViewModel = viewModel(
                    factory = CartViewModelFactory(orderRepository, printerManager)
                )

                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(orderRepository)
                )

                var selectedTab by remember { mutableIntStateOf(0) }
                var showCart by remember { mutableIntStateOf(0) }

                Scaffold(
                    bottomBar = {
                        if (showCart == 0) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Menu") },
                                    label = { Text("Menu") },
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                    label = { Text("History") },
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (showCart) {
                        1 -> CartScreen(
                            viewModel = cartViewModel,
                            menuViewModel = menuViewModel,
                            onNavigateBack = { showCart = 0 },
                            modifier = Modifier.padding(innerPadding)
                        )
                        else -> when (selectedTab) {
                            0 -> MenuScreen(
                                viewModel = menuViewModel,
                                onNavigateToCart = { showCart = 1 },
                                modifier = Modifier.padding(innerPadding)
                            )
                            1 -> HistoryScreen(
                                viewModel = historyViewModel,
                                onNavigateBack = { selectedTab = 0 },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }

        requestPermissionLauncher.launch(permissions.toTypedArray())
    }
}