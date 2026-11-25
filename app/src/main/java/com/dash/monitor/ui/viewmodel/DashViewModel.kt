package com.dash.monitor.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dash.monitor.data.local.dao.AppUsageSummary
import com.dash.monitor.data.local.dao.DataPeriodTotal
import com.dash.monitor.data.local.dao.DataUsageSummary
import com.dash.monitor.data.local.dao.PeriodTotal
import com.dash.monitor.data.local.entity.DeviceSnapshot
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.data.repository.MonitorRepository
import com.dash.monitor.service.MonitorForegroundService
import com.dash.monitor.ui.components.ReportPeriod
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionHelper
import com.dash.monitor.util.PermissionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MonitorRepository(application)
    private val dao = repository.dao

    val todayKey = DateKeys.dayKey()
    val currentMonthKey = DateKeys.monthKey()
    val currentYearKey = DateKeys.yearKey()

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.DAY)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod

    private val _permissionStatus = MutableStateFlow(PermissionHelper.getStatus(application))
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    // Overview flows
    val todayScreenTimeMs: StateFlow<Long> = dao.observeTotalScreenTimeForDay(todayKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthScreenTimeMs: StateFlow<Long> = dao.observeTotalScreenTimeForMonth(currentMonthKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val yearScreenTimeMs: StateFlow<Long> = dao.observeTotalScreenTimeForYear(currentYearKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val latestLocation: StateFlow<LocationRecord?> = dao.observeLatestLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayLocationCount: StateFlow<Int> = dao.observeLocationCountForDay(todayKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalLocationCount: StateFlow<Int> = dao.observeTotalLocationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val latestSnapshot: StateFlow<DeviceSnapshot?> = dao.observeLatestSnapshot()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val snapshotCount: StateFlow<Int> = dao.observeSnapshotCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Period-reactive Screen Time
    val appUsageList: StateFlow<List<AppUsageSummary>> = _selectedPeriod.flatMapLatest { period ->
        when (period) {
            ReportPeriod.DAY -> dao.observeAppUsageForDay(todayKey)
            ReportPeriod.MONTH -> dao.observeAppUsageForMonth(currentMonthKey)
            ReportPeriod.YEAR -> dao.observeAppUsageForYear(currentYearKey)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val screenTimeTrend: StateFlow<List<PeriodTotal>> = _selectedPeriod.flatMapLatest { period ->
        when (period) {
            ReportPeriod.DAY, ReportPeriod.MONTH -> dao.observeDailyScreenTimeInMonth(currentMonthKey)
            ReportPeriod.YEAR -> dao.observeMonthlyScreenTimeInYear(currentYearKey)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Period-reactive Data Usage
    val dataUsageList: StateFlow<List<DataUsageSummary>> = _selectedPeriod.flatMapLatest { period ->
        when (period) {
            ReportPeriod.DAY -> dao.observeDataUsageForDay(todayKey)
            ReportPeriod.MONTH -> dao.observeDataUsageForMonth(currentMonthKey)
            ReportPeriod.YEAR -> dao.observeDataUsageForYear(currentYearKey)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dataUsageTrend: StateFlow<List<DataPeriodTotal>> = _selectedPeriod.flatMapLatest { period ->
        when (period) {
            ReportPeriod.DAY, ReportPeriod.MONTH -> dao.observeDailyDataInMonth(currentMonthKey)
            ReportPeriod.YEAR -> dao.observeMonthlyDataInYear(currentYearKey)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Period-reactive Locations
    val locationList: StateFlow<List<LocationRecord>> = _selectedPeriod.flatMapLatest { period ->
        when (period) {
            ReportPeriod.DAY -> dao.observeLocationsForDay(todayKey)
            ReportPeriod.MONTH -> dao.observeLocationsForMonth(currentMonthKey)
            ReportPeriod.YEAR -> dao.observeLocationsForYear(currentYearKey)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableMonths: StateFlow<List<String>> = dao.observeAvailableMonths()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableYears: StateFlow<List<String>> = dao.observeAvailableYears()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshPermissionsAndSync()
    }

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun refreshPermissionsAndSync() {
        val ctx = getApplication<Application>()
        val status = PermissionHelper.getStatus(ctx)
        _permissionStatus.value = status
        if (status.fineLocationGranted) {
            MonitorForegroundService.start(ctx)
        }
        syncNow()
    }

    fun syncNow() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            repository.syncRecentDays(daysBack = 6)
            _isSyncing.value = false
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            val result = repository.exportLocalBackupJson()
            _exportMessage.value = result.fold(
                onSuccess = { path -> "Saved locally: $path" },
                onFailure = { err -> "Export failed: ${err.message}" }
            )
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}
