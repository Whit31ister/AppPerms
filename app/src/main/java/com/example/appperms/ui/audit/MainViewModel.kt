package com.example.appperms.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appperms.data.AppAuditRepository
import com.example.appperms.data.UserPreferencesRepository
import com.example.appperms.domain.AppInfoModel
import com.example.appperms.domain.RiskCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

enum class AppScope { USER, SYSTEM, ALL }
enum class SortOption { RISK_DESC, RISK_ASC, GRANTED_DESC, GRANTED_ASC, NAME_ASC }

data class AuditFilters(
    val query: String = "",
    val appScope: AppScope = AppScope.ALL,
    val onlyGranted: Boolean = false,
    val dangerousPermissions: Set<String> = emptySet(),
    val sort: SortOption = SortOption.RISK_DESC
)

sealed interface AuditUiState {
    data object Loading : AuditUiState
    data class Error(val message: String) : AuditUiState
    data class Success(val apps: List<AppInfoModel>, val filters: AuditFilters) : AuditUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppAuditRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val apps = MutableStateFlow<List<AppInfoModel>>(emptyList())
    private val filters = MutableStateFlow(AuditFilters())
    private val scanError = MutableStateFlow<String?>(null)
    private val selectedPackages = MutableStateFlow<Set<String>>(emptySet())

    val selected: StateFlow<Set<String>> = selectedPackages

    val uiState: StateFlow<AuditUiState> = combine(apps, filters, scanError) { allApps, currentFilters, error ->
        if (error != null) AuditUiState.Error(error)
        else AuditUiState.Success(applyFilters(allApps, currentFilters), currentFilters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuditUiState.Loading)

    init {
        viewModelScope.launch {
            preferencesRepository.preferences.collect { preferences ->
                filters.value = filters.value.copy(
                    sort = preferences.sort,
                    appScope = preferences.appScope
                )
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            scanError.value = null
            runCatching { repository.scanInstalledApps() }
                .onSuccess { apps.value = it }
                .onFailure { scanError.value = it.message ?: "Unknown scanning error" }
        }
    }

    fun updateQuery(query: String) {
        filters.value = filters.value.copy(query = query)
    }

    fun updateAppScope(scope: AppScope) {
        filters.value = filters.value.copy(appScope = scope)
    }

    fun updateOnlyGranted(onlyGranted: Boolean) {
        filters.value = filters.value.copy(onlyGranted = onlyGranted)
    }

    fun toggleDangerousPermission(permission: String) {
        val selected = filters.value.dangerousPermissions
        filters.value = filters.value.copy(
            dangerousPermissions = if (permission in selected) selected - permission else selected + permission
        )
    }

    fun updateSort(sort: SortOption) {
        filters.value = filters.value.copy(sort = sort)
    }

    fun toggleSelected(packageName: String) {
        selectedPackages.value = if (packageName in selectedPackages.value) {
            selectedPackages.value - packageName
        } else {
            selectedPackages.value + packageName
        }
    }

    fun clearSelection() {
        selectedPackages.value = emptySet()
    }

    private fun applyFilters(apps: List<AppInfoModel>, filters: AuditFilters): List<AppInfoModel> =
        apps.asSequence()
            .filter { app ->
                when (filters.appScope) {
                    AppScope.USER -> !app.isSystemApp
                    AppScope.SYSTEM -> app.isSystemApp
                    AppScope.ALL -> true
                }
            }
            .filter { app ->
                val query = filters.query.trim()
                query.isEmpty() || app.appName.contains(query, true) || app.packageName.contains(query, true)
            }
            .filter { app ->
                filters.dangerousPermissions.isEmpty() ||
                    filters.dangerousPermissions.any { selected ->
                        app.permissions.any { it.name == selected }
                    }
            }
            .map { app ->
                if (!filters.onlyGranted) app
                else app.copy(permissions = app.permissions.filter { it.isGranted })
            }
            .sortedWith(
                when (filters.sort) {
                    SortOption.RISK_DESC -> compareByDescending<AppInfoModel> { it.riskScore }
                    SortOption.RISK_ASC -> compareBy<AppInfoModel> { it.riskScore }
                    SortOption.GRANTED_DESC -> compareByDescending { it.totalPermissionsGranted }
                    SortOption.GRANTED_ASC -> compareBy { it.totalPermissionsGranted }
                    SortOption.NAME_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.appName }
                }
            )
            .toList()
}
