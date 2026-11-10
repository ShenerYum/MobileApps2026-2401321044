package com.budgeteer.app.domain.repository

import com.budgeteer.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun observeUserSettings(): Flow<UserSettings>
    suspend fun getUserSettings(): UserSettings
    suspend fun saveUserSettings(settings: UserSettings)
}
