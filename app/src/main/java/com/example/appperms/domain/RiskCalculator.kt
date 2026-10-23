package com.example.appperms.domain

object RiskCalculator {
    private val criticalPermissions = setOf(
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_PHONE_STATE",
        "android.permission.CALL_PHONE",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    private val normalPermissions = setOf(
        "android.permission.INTERNET",
        "android.permission.BLUETOOTH",
        "android.permission.BLUETOOTH_ADMIN",
        "android.permission.BLUETOOTH_CONNECT",
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.VIBRATE",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.ACCESS_WIFI_STATE",
        "android.permission.FOREGROUND_SERVICE"
    )

    fun tierFor(permission: String): RiskTier =
        when {
            permission in criticalPermissions -> RiskTier.CRITICAL
            permission in normalPermissions -> RiskTier.NORMAL
            else -> RiskTier.NORMAL
        }

    fun score(permissions: List<AppPermissionModel>): Int = permissions.sumOf { permission ->
        when (permission.tier) {
            RiskTier.CRITICAL, RiskTier.DANGEROUS ->
                if (permission.isGranted) 10 else 2
            RiskTier.NORMAL -> if (permission.isGranted) 1 else 0
        }
    }

    fun isFilterableDangerousPermission(permission: String): Boolean =
        permission in criticalPermissions
}
