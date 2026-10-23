package com.example.appperms.domain

import androidx.compose.ui.graphics.ImageBitmap

data class AppInfoModel(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap,
    val isSystemApp: Boolean,
    val totalPermissionsRequested: Int,
    val totalPermissionsGranted: Int,
    val riskScore: Int,
    val permissions: List<AppPermissionModel>
)
