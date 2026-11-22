package com.budgeteer.app.di

import android.content.Context
import androidx.room.Room
import com.budgeteer.app.data.local.database.BudgeteerDatabase
import com.budgeteer.app.data.repository.CategoryRepositoryImpl
import com.budgeteer.app.data.repository.CurrencyRateRepositoryImpl
import com.budgeteer.app.data.repository.FinancialTransactionRunnerImpl
import com.budgeteer.app.data.repository.MonthlyBudgetRepositoryImpl
import com.budgeteer.app.data.repository.SavingsTransferRepositoryImpl
import com.budgeteer.app.data.repository.TransactionRepositoryImpl
import com.budgeteer.app.data.repository.UserSettingsRepositoryImpl
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.CurrencyRateRepository
import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import java.time.Clock
import java.time.ZoneId

class AppContainer(context: Context) {

    val database: BudgeteerDatabase by lazy {
        Room.databaseBuilder(
            context,
            BudgeteerDatabase::class.java,
            "budgeteer.db"
        ).build()
    }

    val financialTransactionRunner: FinancialTransactionRunner by lazy {
        FinancialTransactionRunnerImpl(database)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val currencyRateRepository: CurrencyRateRepository by lazy {
        CurrencyRateRepositoryImpl(database.currencyRateDao())
    }

    val monthlyBudgetRepository: MonthlyBudgetRepository by lazy {
        MonthlyBudgetRepositoryImpl(database.monthlyBudgetDao())
    }

    val savingsTransferRepository: SavingsTransferRepository by lazy {
        SavingsTransferRepositoryImpl(database.savingsTransferDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao())
    }

    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepositoryImpl(database.userSettingsDao())
    }

    val clock: Clock by lazy {
        Clock.systemDefaultZone()
    }

    val zoneId: ZoneId by lazy {
        ZoneId.systemDefault()
    }
}
