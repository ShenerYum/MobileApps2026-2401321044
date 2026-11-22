package com.budgeteer.app

import android.app.Application
import com.budgeteer.app.di.AppContainer

class BudgeteerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
