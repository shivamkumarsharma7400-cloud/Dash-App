package com.dash.monitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
import kotlinx.coroutines.flow.Flow

data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val totalTimeMs: Long,
    val totalLaunches: Int
)

data class DataUsageSummary(
    val packageName: String,
    val appName: String,
    val wifiBytes: Long,
    val mobileBytes: Long
) {
    val totalBytes: Long get() = wifiBytes + mobileBytes
}

data class PeriodTotal(
    val periodKey: String,
    val totalValue: Long
)

data class DataPeriodTotal(
    val periodKey: String,
    val wifiBytes: Long,
    val mobileBytes: Long
)

data class ContactCallSummary(
    val phoneNumber: String,
    val contactName: String,
    val callCount: Int,
    val totalDurationSec: Long,
    val missedCount: Int
)

data class NotificationAppSummary(
    val packageName: String,
    val appName: String,
    val notifCount: Int
)

@Dao
interface MonitorDao {

    // --- Location ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(record: LocationRecord)

    @Query("SELECT * FROM location_records WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun observeLocationsForDay(dateKey: String): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records WHERE monthKey = :monthKey ORDER BY timestamp DESC")
    fun observeLocationsForMonth(monthKey: String): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records WHERE yearKey = :yearKey ORDER BY timestamp DESC LIMIT 500")
    fun observeLocationsForYear(yearKey: String): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records ORDER BY timestamp DESC LIMIT 1")
    fun observeLatestLocation(): Flow<LocationRecord?>

    @Query("SELECT COUNT(*) FROM location_records WHERE dateKey = :dateKey")
    fun observeLocationCountForDay(dateKey: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM location_records")
    fun observeTotalLocationCount(): Flow<Int>

    @Query("SELECT * FROM location_records ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<LocationRecord>

    // --- App Usage (Screen Time) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppUsage(items: List<AppUsageDaily>)

    @Query("""
        SELECT packageName, appName, SUM(foregroundTimeMs) AS totalTimeMs, SUM(launchCount) AS totalLaunches
        FROM app_usage_daily WHERE dateKey = :dateKey
        GROUP BY packageName ORDER BY totalTimeMs DESC
    """)
    fun observeAppUsageForDay(dateKey: String): Flow<List<AppUsageSummary>>

    @Query("""
        SELECT packageName, appName, SUM(foregroundTimeMs) AS totalTimeMs, SUM(launchCount) AS totalLaunches
        FROM app_usage_daily WHERE monthKey = :monthKey
        GROUP BY packageName ORDER BY totalTimeMs DESC
    """)
    fun observeAppUsageForMonth(monthKey: String): Flow<List<AppUsageSummary>>

    @Query("""
        SELECT packageName, appName, SUM(foregroundTimeMs) AS totalTimeMs, SUM(launchCount) AS totalLaunches
        FROM app_usage_daily WHERE yearKey = :yearKey
        GROUP BY packageName ORDER BY totalTimeMs DESC
    """)
    fun observeAppUsageForYear(yearKey: String): Flow<List<AppUsageSummary>>

    @Query("SELECT COALESCE(SUM(foregroundTimeMs), 0) FROM app_usage_daily WHERE dateKey = :dateKey")
    fun observeTotalScreenTimeForDay(dateKey: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(foregroundTimeMs), 0) FROM app_usage_daily WHERE monthKey = :monthKey")
    fun observeTotalScreenTimeForMonth(monthKey: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(foregroundTimeMs), 0) FROM app_usage_daily WHERE yearKey = :yearKey")
    fun observeTotalScreenTimeForYear(yearKey: String): Flow<Long>

    @Query("""
        SELECT dateKey AS periodKey, SUM(foregroundTimeMs) AS totalValue
        FROM app_usage_daily WHERE monthKey = :monthKey
        GROUP BY dateKey ORDER BY dateKey ASC
    """)
    fun observeDailyScreenTimeInMonth(monthKey: String): Flow<List<PeriodTotal>>

    @Query("""
        SELECT monthKey AS periodKey, SUM(foregroundTimeMs) AS totalValue
        FROM app_usage_daily WHERE yearKey = :yearKey
        GROUP BY monthKey ORDER BY monthKey ASC
    """)
    fun observeMonthlyScreenTimeInYear(yearKey: String): Flow<List<PeriodTotal>>

    @Query("SELECT * FROM app_usage_daily ORDER BY dateKey DESC, foregroundTimeMs DESC")
    suspend fun getAllAppUsage(): List<AppUsageDaily>

    // --- Hourly Usage ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHourlyUsage(items: List<HourlyUsageEntity>)

    @Query("SELECT * FROM hourly_usage WHERE dateKey = :dateKey ORDER BY hour ASC")
    fun observeHourlyUsageForDay(dateKey: String): Flow<List<HourlyUsageEntity>>

    // --- App Limits ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppLimit(limit: AppLimitEntity)

    @Query("DELETE FROM app_limits WHERE packageName = :packageName")
    suspend fun deleteAppLimit(packageName: String)

    @Query("SELECT * FROM app_limits")
    fun observeAppLimits(): Flow<List<AppLimitEntity>>

    // --- Data Usage ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDataUsage(items: List<DataUsageDaily>)

    @Query("""
        SELECT packageName, appName,
               SUM(wifiRxBytes + wifiTxBytes) AS wifiBytes,
               SUM(mobileRxBytes + mobileTxBytes) AS mobileBytes
        FROM data_usage_daily WHERE dateKey = :dateKey
        GROUP BY packageName ORDER BY (wifiBytes + mobileBytes) DESC
    """)
    fun observeDataUsageForDay(dateKey: String): Flow<List<DataUsageSummary>>

    @Query("""
        SELECT packageName, appName,
               SUM(wifiRxBytes + wifiTxBytes) AS wifiBytes,
               SUM(mobileRxBytes + mobileTxBytes) AS mobileBytes
        FROM data_usage_daily WHERE monthKey = :monthKey
        GROUP BY packageName ORDER BY (wifiBytes + mobileBytes) DESC
    """)
    fun observeDataUsageForMonth(monthKey: String): Flow<List<DataUsageSummary>>

    @Query("""
        SELECT packageName, appName,
               SUM(wifiRxBytes + wifiTxBytes) AS wifiBytes,
               SUM(mobileRxBytes + mobileTxBytes) AS mobileBytes
        FROM data_usage_daily WHERE yearKey = :yearKey
        GROUP BY packageName ORDER BY (wifiBytes + mobileBytes) DESC
    """)
    fun observeDataUsageForYear(yearKey: String): Flow<List<DataUsageSummary>>

    @Query("""
        SELECT dateKey AS periodKey,
               SUM(wifiRxBytes + wifiTxBytes) AS wifiBytes,
               SUM(mobileRxBytes + mobileTxBytes) AS mobileBytes
        FROM data_usage_daily WHERE monthKey = :monthKey
        GROUP BY dateKey ORDER BY dateKey ASC
    """)
    fun observeDailyDataInMonth(monthKey: String): Flow<List<DataPeriodTotal>>

    @Query("""
        SELECT monthKey AS periodKey,
               SUM(wifiRxBytes + wifiTxBytes) AS wifiBytes,
               SUM(mobileRxBytes + mobileTxBytes) AS mobileBytes
        FROM data_usage_daily WHERE yearKey = :yearKey
        GROUP BY monthKey ORDER BY monthKey ASC
    """)
    fun observeMonthlyDataInYear(yearKey: String): Flow<List<DataPeriodTotal>>

    @Query("SELECT * FROM data_usage_daily ORDER BY dateKey DESC")
    suspend fun getAllDataUsage(): List<DataUsageDaily>

    // --- Calls & Talk Time ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalls(calls: List<CallRecordEntity>)

    @Query("SELECT * FROM call_records WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun observeCallsForDay(dateKey: String): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE monthKey = :monthKey ORDER BY timestamp DESC")
    fun observeCallsForMonth(monthKey: String): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE yearKey = :yearKey ORDER BY timestamp DESC LIMIT 500")
    fun observeCallsForYear(yearKey: String): Flow<List<CallRecordEntity>>

    @Query("""
        SELECT phoneNumber, contactName,
               COUNT(*) AS callCount,
               SUM(durationSeconds) AS totalDurationSec,
               SUM(CASE WHEN callType = 3 THEN 1 ELSE 0 END) AS missedCount
        FROM call_records WHERE dateKey = :dateKey
        GROUP BY phoneNumber ORDER BY totalDurationSec DESC, callCount DESC
    """)
    fun observeTopContactsForDay(dateKey: String): Flow<List<ContactCallSummary>>

    @Query("""
        SELECT phoneNumber, contactName,
               COUNT(*) AS callCount,
               SUM(durationSeconds) AS totalDurationSec,
               SUM(CASE WHEN callType = 3 THEN 1 ELSE 0 END) AS missedCount
        FROM call_records WHERE monthKey = :monthKey
        GROUP BY phoneNumber ORDER BY totalDurationSec DESC, callCount DESC
    """)
    fun observeTopContactsForMonth(monthKey: String): Flow<List<ContactCallSummary>>

    @Query("""
        SELECT phoneNumber, contactName,
               COUNT(*) AS callCount,
               SUM(durationSeconds) AS totalDurationSec,
               SUM(CASE WHEN callType = 3 THEN 1 ELSE 0 END) AS missedCount
        FROM call_records WHERE yearKey = :yearKey
        GROUP BY phoneNumber ORDER BY totalDurationSec DESC, callCount DESC
    """)
    fun observeTopContactsForYear(yearKey: String): Flow<List<ContactCallSummary>>

    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    suspend fun getAllCalls(): List<CallRecordEntity>

    // --- Notifications ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(record: NotificationRecordEntity)

    @Query("SELECT * FROM notification_records WHERE dateKey = :dateKey ORDER BY timestamp DESC LIMIT 150")
    fun observeNotificationsForDay(dateKey: String): Flow<List<NotificationRecordEntity>>

    @Query("""
        SELECT packageName, appName, COUNT(*) AS notifCount
        FROM notification_records WHERE dateKey = :dateKey
        GROUP BY packageName ORDER BY notifCount DESC
    """)
    fun observeNotificationSummaryForDay(dateKey: String): Flow<List<NotificationAppSummary>>

    @Query("""
        SELECT packageName, appName, COUNT(*) AS notifCount
        FROM notification_records WHERE monthKey = :monthKey
        GROUP BY packageName ORDER BY notifCount DESC
    """)
    fun observeNotificationSummaryForMonth(monthKey: String): Flow<List<NotificationAppSummary>>

    @Query("SELECT * FROM notification_records ORDER BY timestamp DESC")
    suspend fun getAllNotifications(): List<NotificationRecordEntity>

    // --- App & Network Events ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppEvent(event: AppEventEntity)

    @Query("SELECT * FROM app_events ORDER BY timestamp DESC LIMIT 50")
    fun observeRecentAppEvents(): Flow<List<AppEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetworkEvent(event: NetworkEventEntity)

    @Query("SELECT * FROM network_events ORDER BY timestamp DESC LIMIT 60")
    fun observeRecentNetworkEvents(): Flow<List<NetworkEventEntity>>

    // --- Device Snapshots ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceSnapshot(snapshot: DeviceSnapshot)

    @Query("SELECT * FROM device_snapshots ORDER BY timestamp DESC LIMIT 1")
    fun observeLatestSnapshot(): Flow<DeviceSnapshot?>

    @Query("SELECT * FROM device_snapshots WHERE dateKey = :dateKey ORDER BY timestamp ASC")
    fun observeSnapshotsForDay(dateKey: String): Flow<List<DeviceSnapshot>>

    @Query("SELECT COUNT(*) FROM device_snapshots")
    fun observeSnapshotCount(): Flow<Int>

    @Query("SELECT * FROM device_snapshots ORDER BY timestamp DESC")
    suspend fun getAllSnapshots(): List<DeviceSnapshot>

    // --- Archive Metadata ---
    @Query("SELECT DISTINCT monthKey FROM app_usage_daily UNION SELECT DISTINCT monthKey FROM location_records UNION SELECT DISTINCT monthKey FROM call_records ORDER BY monthKey DESC")
    fun observeAvailableMonths(): Flow<List<String>>

    @Query("SELECT DISTINCT yearKey FROM app_usage_daily UNION SELECT DISTINCT yearKey FROM location_records UNION SELECT DISTINCT yearKey FROM call_records ORDER BY yearKey DESC")
    fun observeAvailableYears(): Flow<List<String>>
}
