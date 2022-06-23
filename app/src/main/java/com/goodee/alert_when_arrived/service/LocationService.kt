package com.goodee.alert_when_arrived.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*

class LocationService : Service() {
    private val TAG: String = "로그"
    // 현재 장소
    var currentLocation: Location? = null
    // 장소를 요청하는 Client
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    // 장소요청의 자세한 설정
    private val locationRequest = LocationRequest.create().apply {
        fastestInterval = 30*1000L
        interval = 45*1000L
        maxWaitTime = 60*1000L
        priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    }
    // 장소 요청을 받으면 실행될  콜백 메서드
    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                currentLocation?.let { currentLocation ->
                    Log.d(TAG,"LocationService - longtitude : ${currentLocation.longitude}, latitude : ${currentLocation.latitude}")
                    val intent = Intent(LOCATION_SERVICE)
                    val bundle = Bundle()
                    bundle.putParcelable("location", currentLocation)
                    intent.putExtra("location", bundle)
                    LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
                }
            }
        }
    }

    private val binder = LocationBinder()
    inner class LocationBinder: Binder() {
        fun getService(): LocationService {
            Log.d(TAG,"LocationBinder - getService() called")
            return this@LocationService
        }
    }

    override fun onCreate() {
        Log.d(TAG,"LocationService - onCreate() called")
        super.onCreate()
    }
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG,"LocationService - onBind() called")
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return binder
    }
}