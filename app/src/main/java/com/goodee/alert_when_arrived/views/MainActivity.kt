package com.goodee.alert_when_arrived.views

import android.Manifest
import android.content.*
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.goodee.alert_when_arrived.R
import com.goodee.alert_when_arrived.databinding.ActivityMainBinding
import com.goodee.alert_when_arrived.service.LocationService


class MainActivity : AppCompatActivity() {
    private val TAG: String = "로그"
    private lateinit var binding: ActivityMainBinding
    private lateinit var mService: LocationService
    private var mBound: Boolean = false
    // 서비스를 위한 콜백
    private val connection = object : ServiceConnection {
        // 서비스 연결 시 불릴 콜백
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG,"MainActivity - onServiceConnected() called")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocationService.LocationBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG,"MainActivity - onServiceDisconnected() called")
            mBound = false
        }
    }

    private val locationPermissionRequest by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    // Precise location access granted.
                    Toast.makeText(this, "ACCESS_FINE_LOCATION GRANTED", Toast.LENGTH_SHORT).show()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    // Only approximate location access granted.
                    Toast.makeText(this, "ACCESS_COARSE_LOCATION GRANTED", Toast.LENGTH_SHORT).show()
                } else -> {
                    // No location access granted.
                    Toast.makeText(this, "No Location granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"MainActivity - onCreate() called")
        super.onCreate(savedInstanceState )

        // 권한 요청
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

        // 서비스 실행
        val intent = Intent(this, LocationService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // 레이아웃 처리
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val textView = binding.textView
        val button = binding.btn
        // 이벤트 등록
        button.setOnClickListener {
            if (mBound) {
                textView.text = mService.currentLocation.toString()
            }
        }

        // 서비스에서 gps 수정하면 불리는 콜백
        val mMessageReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                p1?.let { intent ->
                    val bundle = intent.getBundleExtra("location")
                    bundle?.let { b ->
                        b["location"]?.let {
                            val location = it as Location
                            binding.textView.text = "longitude : ${location.longitude}\nlatitude : ${location.latitude}"
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter(LOCATION_SERVICE))
    }
}