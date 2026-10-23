package com.example.appperms.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appperms.data.UserPreferences
import com.example.appperms.data.UserPreferencesRepository
import com.example.appperms.ui.audit.AppScope
import com.example.appperms.ui.audit.SortOption
import com.example.appperms.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = repository.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences()
    )

    fun updateTheme(mode: ThemeMode) = update { copy(themeMode = mode) }
    fun updateDynamicColor(enabled: Boolean) = update { copy(dynamicColor = enabled) }
    fun updateSort(sort: SortOption) = update { copy(sort = sort) }
    fun updateScope(scope: AppScope) = update { copy(appScope = scope) }
    fun updateWeights(critical: Int, normal: Int, denied: Int) = update {
        copy(criticalWeight = critical, normalWeight = normal, deniedWeight = denied)
    }

    private fun update(transform: UserPreferences.() -> UserPreferences) {
        viewModelScope.launch { repository.update(transform) }
    }
}
