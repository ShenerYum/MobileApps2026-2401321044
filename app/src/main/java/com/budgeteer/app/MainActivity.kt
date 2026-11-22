package com.budgeteer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.budgeteer.app.presentation.navigation.BudgeteerAppScaffold
import com.budgeteer.app.ui.theme.BudgeteerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgeteerTheme {
                BudgeteerAppScaffold()
            }
        }
    }
}
