package com.budgeteer.app.di

import com.budgeteer.app.data.repository.CategoryRepositoryImpl
import com.budgeteer.app.data.repository.CurrencyRateRepositoryImpl
import com.budgeteer.app.data.repository.MonthlyBudgetRepositoryImpl
import com.budgeteer.app.data.repository.SavingsTransferRepositoryImpl
import com.budgeteer.app.data.repository.TransactionRepositoryImpl
import com.budgeteer.app.data.repository.UserSettingsRepositoryImpl
import com.budgeteer.app.domain.repository.CategoryRepository
import com.budgeteer.app.domain.repository.CurrencyRateRepository
import com.budgeteer.app.domain.repository.MonthlyBudgetRepository
import com.budgeteer.app.domain.repository.SavingsTransferRepository
import com.budgeteer.app.domain.repository.TransactionRepository
import com.budgeteer.app.domain.repository.UserSettingsRepository
import com.budgeteer.app.data.repository.FinancialTransactionRunnerImpl
import com.budgeteer.app.domain.repository.FinancialTransactionRunner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRateRepository(impl: CurrencyRateRepositoryImpl): CurrencyRateRepository

    @Binds
    @Singleton
    abstract fun bindMonthlyBudgetRepository(impl: MonthlyBudgetRepositoryImpl): MonthlyBudgetRepository

    @Binds
    @Singleton
    abstract fun bindSavingsTransferRepository(impl: SavingsTransferRepositoryImpl): SavingsTransferRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(impl: UserSettingsRepositoryImpl): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindFinancialTransactionRunner(impl: FinancialTransactionRunnerImpl): FinancialTransactionRunner
}
