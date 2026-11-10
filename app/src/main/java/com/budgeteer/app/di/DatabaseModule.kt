package com.budgeteer.app.di

import android.content.Context
import androidx.room.Room
import com.budgeteer.app.data.local.dao.CategoryDao
import com.budgeteer.app.data.local.dao.CurrencyRateDao
import com.budgeteer.app.data.local.dao.MonthlyBudgetDao
import com.budgeteer.app.data.local.dao.SavingsTransferDao
import com.budgeteer.app.data.local.dao.TransactionDao
import com.budgeteer.app.data.local.dao.UserSettingsDao
import com.budgeteer.app.data.local.database.BudgeteerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBudgeteerDatabase(
        @ApplicationContext context: Context
    ): BudgeteerDatabase {
        return Room.databaseBuilder(
            context,
            BudgeteerDatabase::class.java,
            "budgeteer.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserSettingsDao(db: BudgeteerDatabase): UserSettingsDao = db.userSettingsDao()

    @Provides
    @Singleton
    fun provideMonthlyBudgetDao(db: BudgeteerDatabase): MonthlyBudgetDao = db.monthlyBudgetDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: BudgeteerDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideTransactionDao(db: BudgeteerDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideSavingsTransferDao(db: BudgeteerDatabase): SavingsTransferDao = db.savingsTransferDao()

    @Provides
    @Singleton
    fun provideCurrencyRateDao(db: BudgeteerDatabase): CurrencyRateDao = db.currencyRateDao()
}
