package com.cordination

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cordination.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mapUri: Uri

    private val locations = listOf(
        Pair(23.778388, 90.380045), // Agargaon
        Pair(23.800206, 90.355138), // Mirpur 1
        Pair(23.783010, 90.347232),  // gabtoli mazar road
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check permissions and fetch location
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchLocation()
        }


        binding.btnShowDirection.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
            mapIntent.setPackage("com.google.android.apps.maps") // Force Google Maps app
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Google Maps app is not installed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { currentLocation: Location? ->
            if (currentLocation != null) {
                val userLatitude = currentLocation.latitude
                val userLongitude = currentLocation.longitude

                // Find the nearest location
                val nearest = locations.minByOrNull { location ->
                    val targetLocation = Location("").apply {
                        latitude = location.first
                        longitude = location.second
                    }
                    currentLocation.distanceTo(targetLocation) // Calculate distance in meters
                }

                // Display the result
                nearest?.let {
                    val distance = Location("").apply {
                        latitude = it.first
                        longitude = it.second

                        mapUri = Uri.parse(
                            "https://www.google.com/maps/dir/?api=1" +
                                    "&origin=$userLatitude,$userLongitude" +
                                    "&destination=$latitude,$longitude" +
                                    "&travelmode=driving"
                        )

                    }.distanceTo(currentLocation)

                    binding.locationTextView.setText("Nearest Location:\n" +
                            "Lat: ${it.first}, Lng: ${it.second}\n" +
                            "Distance: ${"%.2f".format(distance / 1000)} km")
                }
            } else {
                Toast.makeText(this, "Unable to fetch current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchLocation()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}