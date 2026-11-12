package com.budgeteer.app.data.repository

import com.budgeteer.app.data.local.dao.UserSettingsDao
import com.budgeteer.app.data.mapper.toDomain
import com.budgeteer.app.data.mapper.toEntity
import com.budgeteer.app.domain.model.UserSettings
import com.budgeteer.app.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : UserSettingsRepository {

    override fun observeUserSettings(): Flow<UserSettings> {
        return userSettingsDao.observeUserSettings().map { entity ->
            entity?.toDomain() ?: UserSettings()
        }
    }

    override suspend fun getUserSettings(): UserSettings {
        return userSettingsDao.getUserSettings()?.toDomain() ?: UserSettings()
    }

    override suspend fun saveUserSettings(settings: UserSettings) {
        userSettingsDao.upsertUserSettings(settings.toEntity())
    }
}
