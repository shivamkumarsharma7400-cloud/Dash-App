package com.dash.monitor.data.collector

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

data class AppPermissionAudit(
    val packageName: String,
    val appName: String,
    val grantedSensitivePerms: List<String>
)

class PermissionAuditor(private val context: Context) {

    private val watchedPerms = mapOf(
        Manifest.permission.CAMERA to "Camera",
        Manifest.permission.RECORD_AUDIO to "Mic",
        Manifest.permission.ACCESS_FINE_LOCATION to "Location",
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to "Bg Location",
        Manifest.permission.READ_CONTACTS to "Contacts",
        Manifest.permission.READ_CALL_LOG to "Calls",
        Manifest.permission.READ_SMS to "SMS"
    )

    fun scanInstalledApps(): List<AppPermissionAudit> {
        val pm = context.packageManager
        val packages = try {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        } catch (_: Exception) {
            return emptyList()
        }

        val results = mutableListOf<AppPermissionAudit>()
        for (pkgInfo in packages) {
            val appInfo = pkgInfo.applicationInfo ?: continue
            // Focus on user-installed or updated system apps
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystem) continue

            val requested = pkgInfo.requestedPermissions ?: continue
            val flags = pkgInfo.requestedPermissionsFlags ?: continue
            val granted = mutableListOf<String>()

            for (i in requested.indices) {
                val perm = requested[i]
                val label = watchedPerms[perm] ?: continue
                val isGranted = (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                if (isGranted && label !in granted) {
                    granted.add(label)
                }
            }

            if (granted.isNotEmpty()) {
                val label = pm.getApplicationLabel(appInfo).toString()
                results.add(
                    AppPermissionAudit(
                        packageName = pkgInfo.packageName,
                        appName = label,
                        grantedSensitivePerms = granted
                    )
                )
            }
        }
        return results.sortedByDescending { it.grantedSensitivePerms.size }
    }
}
