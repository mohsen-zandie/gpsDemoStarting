package com.komozan.gpstracker

import android.app.Application
import android.location.Location

class MyApplication : Application() {

    private lateinit var singleton: MyApplication
    private var locations = ArrayList<Location>()
    fun getInstance(): MyApplication {
        return singleton
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this
//        locations = ArrayList()
    }

    fun getMyLocations() = locations
    fun setMyLocations(savedLocations: java.util.ArrayList<Location>) {
        locations.clear()
        locations.addAll(savedLocations)
    }
}