package com.example.appperms.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.appperms.ui.about.AboutScreen
import com.example.appperms.ui.audit.AppListScreen
import com.example.appperms.ui.batch.BatchActionsScreen
import com.example.appperms.ui.settings.SettingsScreen

private enum class Destination(val title: String) {
    AUDIT("Audit"),
    BATCH("Batch"),
    SETTINGS("Settings"),
    ABOUT("About")
}

@Composable
fun MainNavigation() {
    var destination by rememberSaveable { mutableStateOf(Destination.AUDIT) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.values().forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Text(item.title.take(1)) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            when (destination) {
                Destination.AUDIT -> AppListScreen()
                Destination.BATCH -> BatchActionsScreen()
                Destination.SETTINGS -> SettingsScreen()
                Destination.ABOUT -> AboutScreen()
            }
        }
    }
}
