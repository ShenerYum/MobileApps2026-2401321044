package com.budgeteer.app.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgeteer.app.presentation.categories.CategoriesScreen
import com.budgeteer.app.presentation.common.components.ErrorState
import com.budgeteer.app.presentation.common.components.LoadingState
import com.budgeteer.app.presentation.common.di.injectViewModel
import com.budgeteer.app.presentation.dashboard.DashboardScreen
import com.budgeteer.app.presentation.savings.SavingsScreen
import com.budgeteer.app.presentation.settings.SettingsScreen
import com.budgeteer.app.presentation.startup.StartupUiState
import com.budgeteer.app.presentation.startup.StartupViewModel
import com.budgeteer.app.presentation.transactions.TransactionsScreen

@Composable
fun BudgeteerAppScaffold(
    startupViewModel: StartupViewModel = injectViewModel()
) {
    val startupState by startupViewModel.uiState.collectAsStateWithLifecycle()

    when (val state = startupState) {
        is StartupUiState.Loading -> {
            LoadingState(message = "Processing monthly budget catch-up...")
        }
        is StartupUiState.Error -> {
            ErrorState(
                message = state.message,
                onRetryClicked = { startupViewModel.runStartupCatchUp() }
            )
        }
        is StartupUiState.Success -> {
            MainNavigationScaffold()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigationScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentScreen = Screen.bottomNavScreens.find { it.route == currentRoute } ?: Screen.Dashboard

    fun navigateToTab(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = currentScreen.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                Screen.bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navigateToTab(screen.route) },
                        icon = {
                            if (screen.icon != null) {
                                Icon(imageVector = screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToTransactions = { navigateToTab(Screen.Transactions.route) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen()
            }
            composable(Screen.Savings.route) {
                SavingsScreen()
            }
            composable(Screen.Categories.route) {
                CategoriesScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
