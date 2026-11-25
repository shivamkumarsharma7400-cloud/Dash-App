package com.dash.monitor

import android.app.Application
import com.dash.monitor.data.local.DashDatabase
import com.dash.monitor.worker.DailySyncWorker

class DashApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DashDatabase.getInstance(this)
        DailySyncWorker.schedule(this)
    }
}
