package com.dash.monitor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dash.monitor.data.local.dao.MonitorDao
import com.dash.monitor.data.local.entity.AppEventEntity
import com.dash.monitor.data.local.entity.AppLimitEntity
import com.dash.monitor.data.local.entity.AppUsageDaily
import com.dash.monitor.data.local.entity.CallRecordEntity
import com.dash.monitor.data.local.entity.DataUsageDaily
import com.dash.monitor.data.local.entity.DeviceSnapshot
import com.dash.monitor.data.local.entity.HourlyUsageEntity
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.data.local.entity.NetworkEventEntity
import com.dash.monitor.data.local.entity.NotificationRecordEntity

@Database(
    entities = [
        LocationRecord::class,
        AppUsageDaily::class,
        DataUsageDaily::class,
        DeviceSnapshot::class,
        CallRecordEntity::class,
        HourlyUsageEntity::class,
        NotificationRecordEntity::class,
        AppEventEntity::class,
        NetworkEventEntity::class,
        AppLimitEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DashDatabase : RoomDatabase() {
    abstract fun monitorDao(): MonitorDao

    companion object {
        @Volatile
        private var INSTANCE: DashDatabase? = null

        fun getInstance(context: Context): DashDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DashDatabase::class.java,
                    "dash_local_monitor.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
