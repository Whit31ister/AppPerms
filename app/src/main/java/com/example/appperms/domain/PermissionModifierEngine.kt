package com.example.appperms.domain

interface PermissionModifierEngine {
    suspend fun setPermission(packageName: String, permission: String, granted: Boolean): Result<Unit>
    suspend fun isAvailable(): Boolean
}

class UnsupportedPermissionModifierEngine : PermissionModifierEngine {
    override suspend fun setPermission(
        packageName: String,
        permission: String,
        granted: Boolean
    ): Result<Unit> = Result.failure(
        UnsupportedOperationException("Shizuku/AppOps integration is not configured")
    )

    override suspend fun isAvailable(): Boolean = false
}
