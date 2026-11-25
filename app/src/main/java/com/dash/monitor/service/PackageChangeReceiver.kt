package com.dash.monitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.dash.monitor.data.local.DashDatabase
import com.dash.monitor.data.local.entity.AppEventEntity
import com.dash.monitor.util.DateKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pkg = intent.data?.schemeSpecificPart ?: return
        val eventType = when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) "UPDATED" else "INSTALLED"
            }
            Intent.ACTION_PACKAGE_REPLACED -> "UPDATED"
            Intent.ACTION_PACKAGE_REMOVED -> {
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
                "REMOVED"
            }
            else -> return
        }

        val appName = try {
            val info = context.packageManager.getApplicationInfo(pkg, 0)
            context.packageManager.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            pkg.substringAfterLast('.')
        }

        val now = System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).launch {
            DashDatabase.getInstance(context).monitorDao().insertAppEvent(
                AppEventEntity(
                    timestamp = now,
                    dateKey = DateKeys.dayKey(now),
                    monthKey = DateKeys.monthKey(now),
                    yearKey = DateKeys.yearKey(now),
                    packageName = pkg,
                    appName = appName,
                    eventType = eventType
                )
            )
        }
    }
}
