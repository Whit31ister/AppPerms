package com.example.appperms.ui.batch

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appperms.domain.AppInfoModel
import com.example.appperms.ui.audit.AuditUiState
import com.example.appperms.ui.audit.MainViewModel

@Composable
fun BatchActionsScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val apps = (state as? AuditUiState.Success)?.apps.orEmpty()
        .filter { it.packageName in selected }
    val totalPermissions = apps.sumOf { it.totalPermissionsRequested }
    val commonPermissions = apps.flatMap { app ->
        app.permissions.filter { it.tier != com.example.appperms.domain.RiskTier.NORMAL }.map { it.name }
    }.groupingBy { it }.eachCount().filterValues { it == apps.size }.keys

    LazyColumn(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Batch Actions", style = MaterialTheme.typography.headlineMedium)
            Text("${apps.size} selected · $totalPermissions requested permissions")
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Common dangerous permissions", style = MaterialTheme.typography.titleMedium)
                    Text(if (commonPermissions.isEmpty()) "No common dangerous permissions" else commonPermissions
                        .joinToString { it.substringAfterLast('.') })
                }
            }
        }
        items(apps, key = { it.packageName }) { app ->
            BatchAppRow(app) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                    }
                )
            }
        }
        if (apps.isNotEmpty()) {
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${apps.first().packageName}")
                            }
                        )
                    }
                ) { Text("Start guided batch wizard") }
            }
        }
    }
}

@Composable
private fun BatchAppRow(app: AppInfoModel, onOpen: () -> Unit) {
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp)) {
            Column {
                Text(app.appName, style = MaterialTheme.typography.titleMedium)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
