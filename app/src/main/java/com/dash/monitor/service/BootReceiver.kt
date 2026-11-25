package com.dash.monitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dash.monitor.util.PermissionHelper
import com.dash.monitor.worker.DailySyncWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DailySyncWorker.schedule(context)
            if (PermissionHelper.getStatus(context).fineLocationGranted) {
                MonitorForegroundService.start(context)
            }
        }
    }
}
