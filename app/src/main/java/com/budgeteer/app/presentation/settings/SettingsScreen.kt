package com.budgeteer.app.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgeteer.app.domain.model.CurrencyCode
import com.budgeteer.app.domain.model.CurrencyRate
import com.budgeteer.app.domain.model.UserSettings
import com.budgeteer.app.presentation.common.components.LoadingState
import com.budgeteer.app.presentation.common.di.injectViewModel
import com.budgeteer.app.ui.theme.BudgeteerTheme
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = injectViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState("Loading Settings...")
        return
    }

    SettingsContent(
        state = state,
        onDisplayCurrencySelected = { viewModel.onDisplayCurrencySelected(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onDisplayCurrencySelected: (CurrencyCode) -> Unit = {}
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Application Settings",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            var isCurrencyDropdownExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Display Currency",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Selected display currency for financial conversions (Stored EUR amounts are preserved).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = isCurrencyDropdownExpanded,
                        onExpandedChange = { isCurrencyDropdownExpanded = !isCurrencyDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.userSettings.displayCurrency.code,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCurrencyDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isCurrencyDropdownExpanded,
                            onDismissRequest = { isCurrencyDropdownExpanded = false }
                        ) {
                            state.availableCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency.code) },
                                    onClick = {
                                        onDisplayCurrencySelected(currency)
                                        isCurrencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cached Currency Exchange Rates",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.cachedRates.isEmpty()) {
                Text(
                    text = "No cached exchange rates available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    items(state.cachedRates) { rate ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${rate.baseCurrency.code} / ${rate.quoteCurrency.code}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = rate.rate.toPlainString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BudgeteerTheme {
        SettingsContent(
            state = SettingsUiState(
                isLoading = false,
                userSettings = UserSettings(displayCurrency = CurrencyCode.EUR),
                cachedRates = listOf(
                    CurrencyRate(
                        id = 1L,
                        baseCurrency = CurrencyCode.EUR,
                        quoteCurrency = CurrencyCode.USD,
                        rate = BigDecimal("1.0850"),
                        rateDate = LocalDate.of(2026, 3, 31),
                        provider = "ECB",
                        fetchedAt = Instant.now()
                    )
                )
            )
        )
    }
}
