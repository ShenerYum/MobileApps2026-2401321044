package com.budgeteer.app.domain.usecase

import com.budgeteer.app.domain.model.Category
import com.budgeteer.app.domain.model.Transaction
import com.budgeteer.app.domain.validation.TransactionValidator
import java.time.ZoneId
import javax.inject.Inject

/**
 * Validates transaction data against business rules and matching category type.
 */
class ValidateTransactionUseCase @Inject constructor() {

    operator fun invoke(
        transaction: Transaction,
        category: Category,
        zoneId: ZoneId = ZoneId.of("UTC")
    ) {
        TransactionValidator.validate(transaction, category, zoneId)
    }
}
