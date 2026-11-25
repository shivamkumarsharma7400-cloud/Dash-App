package com.dash.monitor.data.collector

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import com.dash.monitor.data.local.entity.LocationRecord
import com.dash.monitor.util.DateKeys
import com.dash.monitor.util.PermissionHelper
import java.util.Locale

class LocationTracker(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var activeListener: LocationListener? = null

    @SuppressLint("MissingPermission")
    fun startListening(
        minTimeMs: Long = 120_000L,
        minDistanceMeters: Float = 25f,
        onLocation: (LocationRecord) -> Unit
    ) {
        if (!PermissionHelper.getStatus(context).fineLocationGranted) return
        stopListening()

        val listener = LocationListener { loc ->
            onLocation(toRecord(loc))
        }
        activeListener = listener

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                        provider,
                        minTimeMs,
                        minDistanceMeters,
                        listener,
                        Looper.getMainLooper()
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun stopListening() {
        activeListener?.let {
            try {
                locationManager.removeUpdates(it)
            } catch (_: Exception) {
            }
        }
        activeListener = null
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): LocationRecord? {
        if (!PermissionHelper.getStatus(context).fineLocationGranted) return null
        val providers = locationManager.getProviders(true)
        var best: Location? = null
        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (best == null || loc.accuracy < best.accuracy) {
                    best = loc
                }
            } catch (_: Exception) {
            }
        }
        return best?.let { toRecord(it) }
    }

    private fun toRecord(loc: Location): LocationRecord {
        val ts = if (loc.time > 0) loc.time else System.currentTimeMillis()
        val address = reverseGeocode(loc.latitude, loc.longitude)
        return LocationRecord(
            timestamp = ts,
            dateKey = DateKeys.dayKey(ts),
            monthKey = DateKeys.monthKey(ts),
            yearKey = DateKeys.yearKey(ts),
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracyMeters = loc.accuracy,
            speedMps = loc.speed,
            altitudeMeters = loc.altitude,
            provider = loc.provider ?: "unknown",
            addressLine = address
        )
    }

    private fun reverseGeocode(lat: Double, lng: Double): String? {
        if (!Geocoder.isPresent()) return null
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var result: String? = null
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lng, 1)
                result = list?.firstOrNull()?.getAddressLine(0)
                result
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lng, 1)
                list?.firstOrNull()?.getAddressLine(0)
            }
        } catch (_: Exception) {
            null
        }
    }
}
