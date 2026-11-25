package com.dash.monitor.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dash.monitor.data.local.DashDatabase
import com.dash.monitor.data.local.entity.NotificationRecordEntity
import com.dash.monitor.util.DateKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DashNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notif = sbn ?: return
        val pkg = notif.packageName ?: return
        if (pkg == packageName) return

        val extras = notif.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        if (title.isEmpty() && text.isEmpty()) return

        val ts = notif.postTime.takeIf { it > 0 } ?: System.currentTimeMillis()
        val appName = try {
            val info = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            pkg.substringAfterLast('.')
        }

        scope.launch {
            DashDatabase.getInstance(applicationContext).monitorDao().insertNotification(
                NotificationRecordEntity(
                    timestamp = ts,
                    dateKey = DateKeys.dayKey(ts),
                    monthKey = DateKeys.monthKey(ts),
                    yearKey = DateKeys.yearKey(ts),
                    packageName = pkg,
                    appName = appName,
                    title = title.take(120),
                    text = text.take(240)
                )
            )
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
