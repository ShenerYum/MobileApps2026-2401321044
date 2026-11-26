package com.budgeteer.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.Receipt)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Savings : Screen("savings", "Savings", Icons.Default.Savings)
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavScreens = listOf(Dashboard, Transactions, Analytics, Savings, Categories, Settings)
    }
}
