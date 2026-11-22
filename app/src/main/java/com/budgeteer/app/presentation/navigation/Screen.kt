package com.budgeteer.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Transactions : Screen("transactions", "Transactions", Icons.Default.ReceiptLong)
    data object Savings : Screen("savings", "Savings", Icons.Default.Savings)
    data object Categories : Screen("categories", "Categories", Icons.Default.Category)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavScreens = listOf(Dashboard, Transactions, Savings, Categories, Settings)
    }
}
