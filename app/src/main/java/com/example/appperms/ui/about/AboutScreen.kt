package com.example.appperms.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appperms.BuildConfig

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("About AppPerms", style = MaterialTheme.typography.headlineMedium)
                Text("Permission visibility and risk analysis for Android.")
                Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Developer", style = MaterialTheme.typography.titleLarge)
                    Text("Bio")
                    Text("A clean-architecture Android permission auditor.")
                    Text("GitHub / Social Links")
                    Text("Project source and developer links can be configured here.")
                    Text("License")
                    Text("Licensed under the Apache License 2.0.")
                }
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))
                    )
                }
            ) { Text("View project source") }
        }
    }
}
