package com.budgeteer.app.domain.repository

/**
 * Domain port for executing atomic database operations.
 */
interface FinancialTransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
