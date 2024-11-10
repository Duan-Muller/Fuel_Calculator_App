package com.example.fuelcalculator.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fuelcalculator.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var locationSwitch: SwitchMaterial
    private lateinit var promotionalSwitch: SwitchMaterial

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Location permission granted
                locationSwitch.isChecked = true
                saveLocationPreference(true)
            }
            else -> {
                // Permission denied
                locationSwitch.isChecked = false
                saveLocationPreference(false)

                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        setupClickListeners()
        loadSettings()
    }

    private fun initializeViews() {
        locationSwitch = findViewById(R.id.locationSwitch)
        promotionalSwitch = findViewById(R.id.promotionalSwitch)

        // Initialize switch state based on both permission and saved preference
        val savedLocationPreference = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("location_enabled", false)
        locationSwitch.isChecked = savedLocationPreference
    }

    private fun setupClickListeners() {
        // View App Storage
        findViewById<MaterialButton>(R.id.viewStorageButton).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${packageName}")
            }
            startActivity(intent)
        }

        // Clear Data
        findViewById<MaterialButton>(R.id.clearDataButton).setOnClickListener {
            showClearDataDialog()
        }

        // Location Access
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Don't check permissions here, directly request them
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                saveLocationPreference(false)
            }
        }

        // Promotional Material
        promotionalSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putBoolean("promotional_enabled", isChecked)
                .apply()

            Toast.makeText(
                this,
                if (isChecked) "Opted in to promotional materials"
                else "Opted out of promotional materials",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Back Button
        findViewById<MaterialButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        //Load preferences
        locationSwitch.isChecked = prefs.getBoolean("location_enabled", false)
        promotionalSwitch.isChecked = prefs.getBoolean("promotional_enabled", false)
    }

    private fun checkLocationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is required for this feature. Please enable it in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                locationSwitch.isChecked = false
                saveLocationPreference(false)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear App Data")
            .setMessage("To completely clear all app data including permissions:\n\n1. Click 'Go to Settings'\n2. Tap 'Clear Data'\n3. Return to the app")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNeutralButton("Clear Preferences Only") { _, _ ->
                //Clear SharedPreferences
                getSharedPreferences("settings", MODE_PRIVATE).edit().clear().apply()
                Toast.makeText(this, "App preferences cleared", Toast.LENGTH_SHORT).show()
                loadSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveLocationPreference(enabled: Boolean) {
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putBoolean("location_enabled", enabled)
            .apply()
    }

    override fun onResume() {
        super.onResume()
        // Check if there is permission but switch is off
        val hasPermission = checkLocationPermissions()
        val savedPreference = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("location_enabled", false)

        // If permission true and preference is saved as true, ensure switch is on
        if (hasPermission && savedPreference) {
            locationSwitch.isChecked = true
        }
        // If don't have permission but preference is saved as true, turn it off
        else if (!hasPermission && savedPreference) {
            locationSwitch.isChecked = false
            saveLocationPreference(false)
        }
    }
}