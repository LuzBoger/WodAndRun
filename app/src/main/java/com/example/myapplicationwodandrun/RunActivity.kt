package com.example.myapplicationwodandrun

import CustomInfoWindow
import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class RunActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    private val raceList = mutableListOf<raceData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)

        val dbHelper = SQLHelper(this)
        val readDB = dbHelper.readableDatabase

        val racesData = afficherUtilisateurs(readDB)
        Toast.makeText(this, "allRace : $racesData", Toast.LENGTH_SHORT).show()

        /* Changement de nom vers run */
        val navBar = findViewById<TextView>(R.id.titleNavBar)
        navBar.text ="RUN"

        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // On à déjà la permission
            activateLocation()
        }else{
            // On a pas encore la permission
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                //Afficher un dialogue pourquoi on en a bsn
                // Et quand l'utilisateur fermera ce dialog redemander la permission
            } else{
                //Demander la permission
                requestPermission()
            }
        }
    }
    private fun requestPermission(){
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                    Toast.makeText(this, "FINE OK", Toast.LENGTH_SHORT).show()
                    // On à la permission précise
                    activateLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                    Toast.makeText(this, "COARSE OK", Toast.LENGTH_SHORT).show()
                    // On à la permission
                    activateLocation()
                }
                else -> {
                    // On a aucune permission
                    Toast.makeText(this, "REFUSED", Toast.LENGTH_SHORT).show()

                }
            }
        }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    private fun activateLocation(){
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    Toast.makeText(this, "Dernière position connue : ${location?.latitude}, ${location?.longitude}",
                        Toast.LENGTH_SHORT).show()
                }
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateDistanceMeters(20f)
                .setWaitForAccurateLocation(true)
                .build()
            val locationCallBack = object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        SetGpsPosition(location.latitude, location.longitude)
                        Toast.makeText(
                            this@RunActivity,
                            "Nouvelle position connue :  ${location?.latitude}, ${location?.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Récupérer les données depuis la base de données
                        val dbHelper = SQLHelper(this@RunActivity)
                        val readDB = dbHelper.readableDatabase
                        val cursor = readDB.rawQuery("SELECT date, distance, temps, position FROM races", null)

                        val dateIndex = cursor.getColumnIndex("date")
                        val distanceIndex = cursor.getColumnIndex("distance")
                        val tempsIndex = cursor.getColumnIndex("temps")
                        val positionIndex = cursor.getColumnIndex("position");

                        while (cursor.moveToNext()) {
                            // Accéder aux colonnes par leurs indices
                            val date = cursor.getString(dateIndex)
                            val distance = cursor.getString(distanceIndex)
                            val temps = cursor.getString(tempsIndex)
                            val positionString = cursor.getString(positionIndex)

                            // Diviser la chaîne en latitude et longitude
                            val parts = positionString.split(",")
                            val test ="proute";
                            if (parts.size == 3) {
                                val lat = parts[0].toDouble()
                                val lon = parts[1].toDouble()

                                // Créer un nouveau marqueur pour chaque enregistrement
                                val position = GeoPoint(lat, lon)
                                val race = raceData(date, distance, temps, position)
                                raceList.add(race);
                                val marker = Marker(mapView)
                                marker.position = position
                                marker.icon = ContextCompat.getDrawable(this@RunActivity, R.drawable.pin)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.infoWindow = CustomInfoWindow(R.layout.custom_info_window, mapView, race)

                                mapView.overlays.add(marker)
                            }
                        }

                        cursor.close()
                        mapView.invalidate()
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper())
        }

    }
    private fun SetGpsPosition(latitude: Double, longitude: Double){
            mapView = findViewById(R.id.map)
            mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            mapView.setMultiTouchControls(true);

            Configuration.getInstance().userAgentValue = packageName
            val mapController = mapView.controller
            mapController.setZoom(18.0)

            val geoPoint = GeoPoint(latitude, longitude)
            mapController.setCenter(geoPoint)
            listenToNewMarker();
    }
    private fun listenToNewMarker() {
        val evOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                val nearestMarker = findNearestMarker(p ?: return false)

                if (nearestMarker != null) {
                    // Si un marqueur existe déjà à proximité, affichez la boîte de dialogue pour le modifier
                    val alertDialogBuilder = AlertDialog.Builder(this@RunActivity)
                    alertDialogBuilder.setTitle("Modifier la course")

                    // Charger la mise en page XML de la boîte de dialogue
                    val inflater = layoutInflater
                    val dialogView = inflater.inflate(R.layout.add_race, null)
                    alertDialogBuilder.setView(dialogView)

                    val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)
                    val editTextDistance = dialogView.findViewById<EditText>(R.id.editTextDistance)
                    val editTextTemps = dialogView.findViewById<EditText>(R.id.editTextTemps)

                    // Recherche de la course correspondant au marqueur sélectionné
                    val existingRace = raceList.find { it.position == nearestMarker.position }
                    val updateQuery = """
                    UPDATE races
                    SET date = ?,
                        distance = ?,
                        temps = ?,
                        position = ?
                    WHERE position = ?
                """
                    existingRace?.let {
                        // Pré-remplissez les champs avec les données actuelles de la course
                        editTextDate.setText(it.date)
                        editTextDistance.setText(it.distance.toString())
                        editTextTemps.setText(it.tempsRealise)
                    }
                    val dbHelper = SQLHelper(this@RunActivity)
                    val writeDB = dbHelper.writableDatabase


                    //writeDB.execSQL(updateQuery, arrayOf("2024-02-26", "10.0", "1:30:00", "46.314616759946375,-0.46939194202423096,0.0"))
                    //writeDB.execSQL("INSERT INTO races (date, distance, temps, position) values (date, distance,temps,p)")

                    alertDialogBuilder.setPositiveButton("Modifier") { dialog, _ ->
                        // Mettez à jour les données de la course existante
                        existingRace?.apply {
                            date = editTextDate.text.toString()
                            distance = editTextDistance.text.toString()
                            tempsRealise = editTextTemps.text.toString()
                            writeDB.execSQL(
                                updateQuery,
                                arrayOf(
                                    date,
                                    distance.toString(),
                                    tempsRealise,
                                    nearestMarker.position.toString(),
                                    nearestMarker.position.toString()
                                )
                            )
                        }
                        val snippet = mapView.findViewById<TextView>(R.id.bubble_description)

                        if (existingRace != null) {
                            snippet.text = "Date: ${existingRace.date}\nDistance: ${existingRace.distance} km\nTemps: ${existingRace.tempsRealise}"
                        }
                        existingRace?.let {
                            nearestMarker.infoWindow = CustomInfoWindow(R.layout.custom_info_window, mapView, it)
                            mapView.invalidate()
                        }
                        dialog.dismiss()
                    }
                    alertDialogBuilder.setPositiveButton("Supprimer") { dialog, _ ->
                        existingRace?.let {
                            val deleteQuery = "DELETE FROM races WHERE position = ?"
                            writeDB.execSQL(deleteQuery, arrayOf(nearestMarker.position.toString()))
                            val snippet = mapView.findViewById<TextView>(R.id.bubble_description)
                            snippet.text = "";
                            nearestMarker.remove(mapView);

                            mapView.invalidate();
                            // Mettez à jour l'affichage ou toute autre action nécessaire après la suppression
                        }
                        dialog.dismiss()
                    }

                    alertDialogBuilder.setNegativeButton("Annuler") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else {
                    // Si aucun marqueur n'existe à proximité, ajoutez un nouveau marqueur
                    val alertDialogBuilder = AlertDialog.Builder(this@RunActivity)
                    alertDialogBuilder.setTitle("Ajouter une nouvelle course")

                    // Charger la mise en page XML de la boîte de dialogue
                    val inflater = layoutInflater
                    val dialogView = inflater.inflate(R.layout.add_race, null)
                    alertDialogBuilder.setView(dialogView)

                    val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)
                    val editTextDistance = dialogView.findViewById<EditText>(R.id.editTextDistance)
                    val editTextTemps = dialogView.findViewById<EditText>(R.id.editTextTemps)

                    alertDialogBuilder.setPositiveButton("Ajouter") { dialog, _ ->
                        val date = editTextDate.text.toString()
                        val distance = editTextDistance.text.toString()
                        val temps = editTextTemps.text.toString()

                        // Ajouter une nouvelle course à la liste (vous pouvez remplacer les valeurs par celles que vous avez)
                        val newRace = raceData(date, distance, temps, p)
                        raceList.add(newRace)
                        /* Ajout en base de donnée : */
                        val dbHelper = SQLHelper(this@RunActivity)
                        val writeDB = dbHelper.writableDatabase
                        writeDB.execSQL("INSERT INTO races (date, distance, temps, position) values (?,?,?,?)",arrayOf(date, distance.toString(), temps, p.toString()))
                        // Code existant pour créer un nouveau marqueur
                        val startMarker = Marker(mapView)
                        startMarker.position = p

                        val drawable = ContextCompat.getDrawable(this@RunActivity, R.drawable.pin)
                        startMarker.icon = drawable

                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        startMarker.infoWindow = CustomInfoWindow(R.layout.custom_info_window, mapView, newRace)
                        mapView.overlays.add(startMarker)
                        mapView.invalidate()

                        dialog.dismiss()
                    }
                    alertDialogBuilder.setNegativeButton("Annuler") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }

                return true
            }
        })
        mapView.overlays.add(evOverlay)
    }

    /* Fonction permmetant de chercher le point le plus proche */

    private fun findNearestMarker(longPressPoint: GeoPoint): Marker? {
        val screenWidth = mapView.width.toDouble()
        val screenHeight = mapView.height.toDouble()

        // Calculer la distance en SP correspondant à 20 pixels sur l'écran (tolérance en SP)
        val toleranceInSP = calculateToleranceInSP(screenWidth, screenHeight)

        val nearestMarkers = mapView.overlays
            .filterIsInstance(Marker::class.java)
            .filter { it.alpha.toDouble() == 1.0 && it.position.distanceToAsDouble(longPressPoint) <= toleranceInSP }

        return nearestMarkers.minByOrNull { it.position.distanceToAsDouble(longPressPoint) }
    }

    private fun calculateToleranceInSP(screenWidth: Double, screenHeight: Double): Double {
        val screenDensity = resources.displayMetrics.density
        val toleranceInPixels = 40 // Tolérance en pixels
        val pixelsPerSP = screenDensity.toDouble() // Nombre de pixels par SP (en fonction de la densité de l'écran)

        // Convertir la tolérance de pixels en unité de distance spatiale (SP)
        return toleranceInPixels / pixelsPerSP
    }
    private fun afficherUtilisateurs(database: SQLiteDatabase): String {
        val table = "races"
        val columns = arrayOf("date", "distance","temps","position");

        val cursor: Cursor = database.query(table, columns, null, null, null, null, null)
        cursor.moveToFirst()

        val raceData = StringBuilder()
        while (!cursor.isAfterLast) {
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val distance = cursor.getString(cursor.getColumnIndexOrThrow("distance"))
            val temps = cursor.getString(cursor.getColumnIndexOrThrow("temps"))
            val position = cursor.getString(cursor.getColumnIndexOrThrow("position"))
            raceData.append("$date $distance $temps $position\n")
            cursor.moveToNext()
        }

        cursor.close()
        return raceData.toString()
    }
}