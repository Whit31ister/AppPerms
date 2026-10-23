package com.example.appperms.ui.audit

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appperms.domain.AppInfoModel
import com.example.appperms.domain.AppPermissionModel
import com.example.appperms.domain.RiskTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppInfoModel?>(null) }
    var selectionMode by remember { mutableStateOf(false) }

    when (val current = state) {
        AuditUiState.Loading -> CenteredMessage("Scanning installed apps…")
        is AuditUiState.Error -> CenteredMessage("Could not scan apps: ${current.message}")
        is AuditUiState.Success -> {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("AppPerms") },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = current.filters.query,
                        onValueChange = viewModel::updateQuery,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search apps or packages") }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${current.apps.size} apps", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showFilters = true }) { Text("Filter & sort") }
                            OutlinedButton(onClick = { selectionMode = !selectionMode }) {
                                Text("Select")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (current.apps.isEmpty()) {
                        CenteredMessage("No apps match the current filters", Modifier.weight(1f))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(current.apps, key = { it.packageName }) { app ->
                                AppCardItem(
                                    app = app,
                                    selected = app.packageName in selected,
                                    onClick = {
                                        if (selectionMode) viewModel.toggleSelected(app.packageName)
                                        else selectedApp = app
                                    },
                                    onLongClick = {
                                        selectionMode = true
                                        viewModel.toggleSelected(app.packageName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters && state is AuditUiState.Success) {
        FilterSheet(
            filters = (state as AuditUiState.Success).filters,
            onDismiss = { showFilters = false },
            onScopeChanged = viewModel::updateAppScope,
            onOnlyGrantedChanged = viewModel::updateOnlyGranted,
            onPermissionToggled = viewModel::toggleDangerousPermission,
            onSortChanged = viewModel::updateSort
        )
    }

    selectedApp?.let { app ->
        AppDetailSheet(
            app = app,
            onDismiss = { selectedApp = null }
        )
    }
}

@Composable
private fun AppCardItem(
    app: AppInfoModel,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(52.dp))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(app.appName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Text(
                    "${app.totalPermissionsGranted} granted · ${app.totalPermissionsRequested} requested",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            RiskBadge(app.riskScore)
        }
        if (selected) Text("Selected for batch actions", modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
    }
}

@Composable
private fun RiskBadge(score: Int) {
    val color = when {
        score >= 30 -> Color(0xFFC62828)
        score >= 10 -> Color(0xFFEF6C00)
        else -> Color(0xFF2E7D32)
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text("$score", color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    filters: AuditFilters,
    onDismiss: () -> Unit,
    onScopeChanged: (AppScope) -> Unit,
    onOnlyGrantedChanged: (Boolean) -> Unit,
    onPermissionToggled: (String) -> Unit,
    onSortChanged: (SortOption) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Filters & sorting", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text("Apps", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppScope.values().forEach { scope ->
                    FilterChip(
                        selected = filters.appScope == scope,
                        onClick = { onScopeChanged(scope) },
                        label = { Text(scope.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = filters.onlyGranted, onCheckedChange = onOnlyGrantedChanged)
                Text("Show only granted permissions")
            }
            Text("Dangerous permissions", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.RECORD_AUDIO", "android.permission.READ_CONTACTS")
                    .forEach { permission ->
                        FilterChip(
                            selected = permission in filters.dangerousPermissions,
                            onClick = { onPermissionToggled(permission) },
                            label = { Text(permission.substringAfterLast('.')) }
                        )
                    }
            }
            Spacer(Modifier.height(12.dp))
            Text("Sort by", style = MaterialTheme.typography.titleMedium)
            SortOption.values().forEach { option ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSortChanged(option) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = filters.sort == option, onClick = { onSortChanged(option) })
                    Text(option.label())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailSheet(app: AppInfoModel, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(56.dp))
                Column(Modifier.padding(start = 12.dp)) {
                    Text(app.appName, style = MaterialTheme.typography.headlineSmall)
                    Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Permissions", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(app.permissions, key = { it.name }) { permission ->
                    PermissionRow(permission)
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${app.packageName}")
                        }
                    )
                }
            ) { Text("Open system app settings") }
        }
    }
}

@Composable
private fun PermissionRow(permission: AppPermissionModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(9.dp).clip(CircleShape).background(
                if (permission.isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
            )
        )
        Column(Modifier.padding(start = 10.dp)) {
            Text(permission.name.substringAfterLast('.'), style = MaterialTheme.typography.bodyMedium)
            Text(
                if (permission.isGranted) "Granted" else "Denied",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CenteredMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, modifier = Modifier.padding(24.dp))
    }
}

private fun SortOption.label(): String = when (this) {
    SortOption.RISK_DESC -> "Risk score (highest first)"
    SortOption.RISK_ASC -> "Risk score (lowest first)"
    SortOption.GRANTED_DESC -> "Granted permissions (most first)"
    SortOption.GRANTED_ASC -> "Granted permissions (fewest first)"
    SortOption.NAME_ASC -> "App name (A–Z)"
}
