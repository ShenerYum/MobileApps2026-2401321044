package com.budgeteer.app.presentation.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.CategoryType
import com.budgeteer.app.presentation.common.components.EmptyState
import com.budgeteer.app.presentation.common.components.LoadingState
import com.budgeteer.app.presentation.common.di.injectViewModel
import com.budgeteer.app.ui.theme.BudgeteerTheme

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = injectViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState("Loading Categories...")
        return
    }

    if (state.isAddCategoryDialogOpen) {
        AddCategoryDialog(
            type = state.selectedType,
            onDismiss = { viewModel.onDismissAddCategoryDialog() },
            onConfirm = { name, icon, color -> viewModel.onAddCategory(name, icon, color) }
        )
    }

    CategoriesContent(
        state = state,
        onTypeSelected = { viewModel.onTypeSelected(it) },
        onOpenAddCategoryDialog = { viewModel.onOpenAddCategoryDialog() },
        onArchiveCategory = { viewModel.onArchiveCategory(it) }
    )
}

@Composable
fun CategoriesContent(
    state: CategoriesUiState,
    onTypeSelected: (CategoryType) -> Unit = {},
    onOpenAddCategoryDialog: () -> Unit = {},
    onArchiveCategory: (Long) -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenAddCategoryDialog) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Category Type Selector
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = state.selectedType == CategoryType.EXPENSE,
                    onClick = { onTypeSelected(CategoryType.EXPENSE) },
                    label = { Text("Expense Categories") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                FilterChip(
                    selected = state.selectedType == CategoryType.INCOME,
                    onClick = { onTypeSelected(CategoryType.INCOME) },
                    label = { Text("Income Categories") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.categories.isEmpty()) {
                EmptyState(
                    message = "No active ${state.selectedType.name.lowercase()} categories.",
                    actionLabel = "Add Category",
                    onActionClicked = onOpenAddCategoryDialog
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.categories,
                        key = { it.id }
                    ) { category ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { onArchiveCategory(category.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Archive Category",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    type: CategoryType,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${type.name.lowercase().replaceFirstChar { it.uppercase() }} Category") },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    errorMsg = null
                },
                label = { Text("Category Name") },
                singleLine = true,
                isError = errorMsg != null,
                supportingText = errorMsg?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onConfirm(nameInput.trim(), "ic_folder", "#6200EE")
                    } else {
                        errorMsg = "Category name cannot be empty"
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

@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    BudgeteerTheme {
        CategoriesContent(
            state = CategoriesUiState(
                isLoading = false,
                selectedType = CategoryType.EXPENSE,
                categories = listOf(
                    Category(1L, "Food & Groceries", CategoryType.EXPENSE, "ic_food", "#FF0000"),
                    Category(2L, "Transport", CategoryType.EXPENSE, "ic_bus", "#0000FF")
                )
            )
        )
    }
}
