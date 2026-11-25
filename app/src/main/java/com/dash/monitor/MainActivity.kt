package com.dash.monitor

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dash.monitor.ui.screens.DataUsageScreen
import com.dash.monitor.ui.screens.LocationScreen
import com.dash.monitor.ui.screens.OverviewScreen
import com.dash.monitor.ui.screens.ReportsScreen
import com.dash.monitor.ui.screens.ScreenTimeScreen
import com.dash.monitor.ui.theme.DashSpace
import com.dash.monitor.ui.theme.DashTheme
import com.dash.monitor.ui.theme.LocalDashPalette
import com.dash.monitor.ui.viewmodel.DashViewModel

enum class DashTab(val label: String) {
    OVERVIEW("HOME"),
    SCREEN("SCREEN"),
    DATA("DATA"),
    LOCATION("MAP"),
    REPORTS("VAULT")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: DashViewModel = viewModel()
            val isDark by vm.isDarkTheme.collectAsState()
            DashTheme(darkTheme = isDark) {
                DashAppRoot(vm = vm)
            }
        }
    }
}

@Composable
private fun DashAppRoot(vm: DashViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentTab by remember { mutableStateOf(DashTab.OVERVIEW) }

    val period by vm.selectedPeriod.collectAsState()
    val permissionStatus by vm.permissionStatus.collectAsState()
    val isSyncing by vm.isSyncing.collectAsState()
    val isDarkTheme by vm.isDarkTheme.collectAsState()

    val todayScreenTime by vm.todayScreenTimeMs.collectAsState()
    val monthScreenTime by vm.monthScreenTimeMs.collectAsState()
    val yearScreenTime by vm.yearScreenTimeMs.collectAsState()
    val appUsage by vm.appUsageList.collectAsState()
    val screenTrend by vm.screenTimeTrend.collectAsState()
    val dataUsage by vm.dataUsageList.collectAsState()
    val dataTrend by vm.dataUsageTrend.collectAsState()
    val locations by vm.locationList.collectAsState()
    val latestLocation by vm.latestLocation.collectAsState()
    val todayLocationCount by vm.todayLocationCount.collectAsState()
    val totalLocationCount by vm.totalLocationCount.collectAsState()
    val latestSnapshot by vm.latestSnapshot.collectAsState()
    val snapshotCount by vm.snapshotCount.collectAsState()
    val availableMonths by vm.availableMonths.collectAsState()
    val availableYears by vm.availableYears.collectAsState()
    val exportMessage by vm.exportMessage.collectAsState()

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        vm.refreshPermissionsAndSync()
    }

    // Refresh permissions when user returns from System Usage Access Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshPermissionsAndSync()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val openUsageSettings: () -> Unit = {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    val requestLocationPermissions: () -> Unit = {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        locationLauncher.launch(perms.toTypedArray())
    }

    val palette = LocalDashPalette.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = palette.background,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(palette.background)) {
                HorizontalDivider(thickness = 1.dp, color = palette.hairline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DashTab.entries.forEach { tab ->
                        val selected = tab == currentTab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) palette.invertedBg else palette.background)
                                .clickable { currentTab = tab }
                                .padding(vertical = DashSpace.lg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) palette.invertedInk else palette.inkSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                DashTab.OVERVIEW -> OverviewScreen(
                    todayKey = vm.todayKey,
                    todayScreenTimeMs = todayScreenTime,
                    monthScreenTimeMs = monthScreenTime,
                    yearScreenTimeMs = yearScreenTime,
                    todayData = dataUsage,
                    latestLocation = latestLocation,
                    todayLocationCount = todayLocationCount,
                    latestSnapshot = latestSnapshot,
                    permissionStatus = permissionStatus,
                    isSyncing = isSyncing,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = vm::toggleTheme,
                    onSyncNow = vm::syncNow,
                    onRequestUsageAccess = openUsageSettings,
                    onRequestLocationAccess = requestLocationPermissions
                )
                DashTab.SCREEN -> ScreenTimeScreen(
                    period = period,
                    onSelectPeriod = vm::selectPeriod,
                    apps = appUsage,
                    trend = screenTrend
                )
                DashTab.DATA -> DataUsageScreen(
                    period = period,
                    onSelectPeriod = vm::selectPeriod,
                    items = dataUsage,
                    trend = dataTrend
                )
                DashTab.LOCATION -> LocationScreen(
                    period = period,
                    onSelectPeriod = vm::selectPeriod,
                    locations = locations
                )
                DashTab.REPORTS -> ReportsScreen(
                    availableMonths = availableMonths,
                    availableYears = availableYears,
                    totalLocationCount = totalLocationCount,
                    snapshotCount = snapshotCount,
                    latestSnapshot = latestSnapshot,
                    permissionStatus = permissionStatus,
                    exportMessage = exportMessage,
                    onExportBackup = vm::exportBackup,
                    onRequestUsageAccess = openUsageSettings,
                    onRequestLocationAccess = requestLocationPermissions
                )
            }
        }
    }
}
