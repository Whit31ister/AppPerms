package com.example.appperms.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appperms.ui.audit.AppScope
import com.example.appperms.ui.audit.SortOption
import com.example.appperms.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val sort: SortOption = SortOption.RISK_DESC,
    val appScope: AppScope = AppScope.ALL,
    val criticalWeight: Int = 10,
    val normalWeight: Int = 1,
    val deniedWeight: Int = 2
)

private val Context.userPreferencesDataStore by preferencesDataStore("user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val sort = stringPreferencesKey("sort")
        val scope = stringPreferencesKey("scope")
        val criticalWeight = intPreferencesKey("critical_weight")
        val normalWeight = intPreferencesKey("normal_weight")
        val deniedWeight = intPreferencesKey("denied_weight")
    }

    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { values ->
        values.toUserPreferences()
    }

    suspend fun update(transform: UserPreferences.() -> UserPreferences) {
        context.userPreferencesDataStore.edit { values ->
            val next = values.toUserPreferences().transform()
            values[Keys.theme] = next.themeMode.name
            values[Keys.dynamicColor] = next.dynamicColor
            values[Keys.sort] = next.sort.name
            values[Keys.scope] = next.appScope.name
            values[Keys.criticalWeight] = next.criticalWeight.coerceIn(0, 100)
            values[Keys.normalWeight] = next.normalWeight.coerceIn(0, 100)
            values[Keys.deniedWeight] = next.deniedWeight.coerceIn(0, 100)
        }
    }

    private fun androidx.datastore.preferences.core.Preferences.toUserPreferences() =
        UserPreferences(
            themeMode = this[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            dynamicColor = this[Keys.dynamicColor] ?: true,
            sort = this[Keys.sort]?.let { runCatching { SortOption.valueOf(it) }.getOrNull() }
                ?: SortOption.RISK_DESC,
            appScope = this[Keys.scope]?.let { runCatching { AppScope.valueOf(it) }.getOrNull() }
                ?: AppScope.ALL,
            criticalWeight = this[Keys.criticalWeight] ?: 10,
            normalWeight = this[Keys.normalWeight] ?: 1,
            deniedWeight = this[Keys.deniedWeight] ?: 2
        )
}
