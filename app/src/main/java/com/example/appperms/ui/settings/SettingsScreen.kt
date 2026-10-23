package com.example.appperms.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appperms.ui.audit.AppScope
import com.example.appperms.ui.audit.SortOption
import com.example.appperms.ui.theme.ThemeMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var critical by remember(preferences.criticalWeight) { mutableIntStateOf(preferences.criticalWeight) }
    var normal by remember(preferences.normalWeight) { mutableIntStateOf(preferences.normalWeight) }
    var denied by remember(preferences.deniedWeight) { mutableIntStateOf(preferences.deniedWeight) }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium) }
        item {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.values().forEach { mode ->
                    FilterChip(
                        selected = preferences.themeMode == mode,
                        onClick = { viewModel.updateTheme(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(preferences.dynamicColor, viewModel::updateDynamicColor)
                Text("Use Dynamic Monet colors (Android 12+)")
            }
        }
        item {
            Text("Startup defaults", style = MaterialTheme.typography.titleMedium)
            Text("App visibility")
            AppScope.values().forEach { scope ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(preferences.appScope == scope, { viewModel.updateScope(scope) })
                    Text(scope.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
            Text("Sort order")
            SortOption.values().forEach { sort ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(preferences.sort == sort, { viewModel.updateSort(sort) })
                    Text(sort.label())
                }
            }
        }
        item {
            Text("Risk engine weights", style = MaterialTheme.typography.titleMedium)
            WeightField("Critical granted", critical) { critical = it; viewModel.updateWeights(critical, normal, denied) }
            WeightField("Normal granted", normal) { normal = it; viewModel.updateWeights(critical, normal, denied) }
            WeightField("Denied critical", denied) { denied = it; viewModel.updateWeights(critical, normal, denied) }
        }
    }
}

@Composable
private fun WeightField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { it.toIntOrNull()?.let(onValueChange) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

private fun SortOption.label(): String = when (this) {
    SortOption.RISK_DESC -> "Risk score (highest first)"
    SortOption.RISK_ASC -> "Risk score (lowest first)"
    SortOption.GRANTED_DESC -> "Granted permissions (most first)"
    SortOption.GRANTED_ASC -> "Granted permissions (fewest first)"
    SortOption.NAME_ASC -> "App name (A-Z)"
}
