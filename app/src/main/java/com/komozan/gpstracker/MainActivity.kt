package com.komozan.gpstracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.komozan.gpstracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val PERMISSION_FINE_LOCATION: Int = 99
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val DEFAULT_UPDATE_INTERVAL = 30L
    private val FAST_UPDATE_INTERVAL = 30L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding?.root
        setContentView(view)
        initLocation()
        initViews()
        updateGPS()
    }

    private fun initViews() {
        binding?.apply {
            swGps.setOnClickListener {
                if (swGps.isChecked) {
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    tvSensor.text = getString(R.string.using_gps_sensor)
                } else {
                    locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                    tvSensor.text = getString(R.string.using_tower_wifi)
                }
            }
        }
    }

    private fun initLocation() {
        locationRequest = LocationRequest.create()
            .setInterval(DEFAULT_UPDATE_INTERVAL * 1000)
            .setFastestInterval(FAST_UPDATE_INTERVAL * 1000)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    }

    private fun updateGPS() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(MainActivity@ this)
        getPermission()
//        getCurrentLocation()
//        updateUi(0)
    }

    private fun getPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                updateUIValues(location)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_FINE_LOCATION
                )
            }
        }
    }

    private fun updateUIValues(location: Location) {
        binding?.apply {
            tvLat.text = location.latitude.toString()
            tvLon.text = location.longitude.toString()
            tvAccuracy.text = location.accuracy.toString()

            if (location.hasAltitude()) {
                tvAltitude.text = location.altitude.toString()
            } else {
                tvAltitude.text = getString(R.string.not_available)
            }

            if (location.hasSpeed()) {
                tvSpeed.text = location.speed.toString()
            } else {
                tvSpeed.text = getString(R.string.not_available)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS()
                } else {
                    Toast.makeText(this, "this app requires permission", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}