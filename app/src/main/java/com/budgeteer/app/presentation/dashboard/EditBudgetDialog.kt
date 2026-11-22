package com.budgeteer.app.presentation.dashboard

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import java.math.BigDecimal

@Composable
fun EditBudgetDialog(
    initialBaseAmount: BigDecimal,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal) -> Unit
) {
    var amountInput by remember { mutableStateOf(initialBaseAmount.toPlainString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Monthly Base Budget") },
        text = {
            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it
                    errorMessage = null
                },
                label = { Text("Base Budget (EUR)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountInput.toBigDecimalOrNull()
                    if (parsed != null && parsed >= BigDecimal.ZERO) {
                        onConfirm(parsed)
                    } else {
                        errorMessage = "Please enter a valid positive budget amount"
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
