package com.komozan.gpstracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.komozan.gpstracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val PERMISSION_FINE_LOCATION: Int = 99
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var currentLocation : Location
    private var savedLocations = ArrayList<Location>()

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

            swLocationsupdates.setOnClickListener {
                if (swLocationsupdates.isChecked) {
                    startLocationUpdates()
                } else {
                    stopLocationUpdates()
                }
            }

            btnNewWayPoints.setOnClickListener {
                savedLocations.clear()
                savedLocations.addAll((application as MyApplication).getMyLocations())
                savedLocations.add(currentLocation)
                (application as MyApplication).setMyLocations(savedLocations)
                binding?.tvWaypoints?.text = savedLocations.size.toString()
            }

            btnShowWayPointList.setOnClickListener {
                Log.d("KoMoZan", "initViews: ${(application as MyApplication).getMyLocations()}")
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
    }

    private fun getPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
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
        val geocoder  = Geocoder(MainActivity@this)
        try {
             val addresses: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            binding?.tvAddress?.text = addresses[0].getAddressLine(0)
        }catch (e:Exception){
            binding?.tvAddress?.text = "Unable to get address"
        }

        savedLocations.clear()
        savedLocations.addAll((application as MyApplication).getMyLocations())
        savedLocations.add(currentLocation)
        (application as MyApplication).setMyLocations(savedLocations)
        binding?.tvWaypoints?.text = savedLocations.size.toString()
    }

    private fun startLocationUpdates() {
        binding?.tvUpdates?.text = getString(R.string.tracking_location)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            getLocationCallBack(),
            Looper.myLooper()!!
        )
    }

    private fun getLocationCallBack() = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            updateUIValues(locationResult.lastLocation)
        }
    }

    private fun stopLocationUpdates() {
        binding?.apply {
            tvUpdates.text = getString(R.string.not_tracking_location)
            tvLat.text = getString(R.string.not_tracking_location)
            tvLon.text = getString(R.string.not_tracking_location)
            tvSpeed.text = getString(R.string.not_tracking_location)
            tvAddress.text = getString(R.string.not_tracking_location)
            tvAccuracy.text = getString(R.string.not_tracking_location)
            tvAltitude.text = getString(R.string.not_tracking_location)
            tvSensor.text = getString(R.string.not_tracking_location)
        }
        fusedLocationProviderClient.removeLocationUpdates(getLocationCallBack())
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