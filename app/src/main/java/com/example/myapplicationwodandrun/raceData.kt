package com.example.myapplicationwodandrun

import org.osmdroid.util.GeoPoint

data class raceData(var date: String, var distance: String, var tempsRealise: String, val position: GeoPoint?) {

}