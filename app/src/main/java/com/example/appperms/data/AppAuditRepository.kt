package com.example.appperms.data

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.appperms.domain.AppInfoModel
import com.example.appperms.domain.AppPermissionModel
import com.example.appperms.domain.RiskCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AppAuditRepository {
    suspend fun scanInstalledApps(): List<AppInfoModel>
}

class DefaultAppAuditRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AppAuditRepository {
    override suspend fun scanInstalledApps(): List<AppInfoModel> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { applicationInfo ->
                val packageInfo = runCatching {
                    packageManager.getPackageInfo(
                        applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS
                    )
                }.getOrNull() ?: return@mapNotNull null
                val requested = packageInfo.requestedPermissions.orEmpty()
                val flags = packageInfo.requestedPermissionsFlags ?: intArrayOf()
                val permissions = requested.mapIndexed { index, name ->
                    AppPermissionModel(
                        name = name,
                        tier = RiskCalculator.tierFor(name),
                        isGranted = ((flags.getOrNull(index) ?: 0) and
                            PackageInfoCompat.REQUESTED_PERMISSION_GRANTED) != 0
                    )
                }
                val icon = applicationInfo.loadIcon(packageManager).toBitmap(
                    width = 96,
                    height = 96
                ).asImageBitmap()
                AppInfoModel(
                    packageName = packageInfo.packageName,
                    appName = applicationInfo.loadLabel(packageManager).toString(),
                    icon = icon,
                    isSystemApp = applicationInfo.flags.and(
                        android.content.pm.ApplicationInfo.FLAG_SYSTEM
                    ) != 0,
                    totalPermissionsRequested = permissions.size,
                    totalPermissionsGranted = permissions.count { it.isGranted },
                    riskScore = RiskCalculator.score(permissions),
                    permissions = permissions
                )
            }
    }
}

private object PackageInfoCompat {
    const val REQUESTED_PERMISSION_GRANTED = 2
}
