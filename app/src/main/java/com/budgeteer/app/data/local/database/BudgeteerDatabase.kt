package com.budgeteer.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budgeteer.app.data.local.dao.CategoryDao
import com.budgeteer.app.data.local.dao.CurrencyRateDao
import com.budgeteer.app.data.local.dao.MonthlyBudgetDao
import com.budgeteer.app.data.local.dao.SavingsTransferDao
import com.budgeteer.app.data.local.dao.TransactionDao
import com.budgeteer.app.data.local.dao.UserSettingsDao
import com.budgeteer.app.data.local.entity.CategoryEntity
import com.budgeteer.app.data.local.entity.CurrencyRateEntity
import com.budgeteer.app.data.local.entity.MonthlyBudgetEntity
import com.budgeteer.app.data.local.entity.SavingsTransferEntity
import com.budgeteer.app.data.local.entity.TransactionEntity
import com.budgeteer.app.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        UserSettingsEntity::class,
        MonthlyBudgetEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        SavingsTransferEntity::class,
        CurrencyRateEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class BudgeteerDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun monthlyBudgetDao(): MonthlyBudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsTransferDao(): SavingsTransferDao
    abstract fun currencyRateDao(): CurrencyRateDao
}
