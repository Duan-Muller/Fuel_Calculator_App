package com.example.fuelcalculator.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fuelcalculator.R
import com.example.fuelcalculator.ui.calculator.CalculatorFragment
import com.example.fuelcalculator.ui.common.BottomNavHelper
import com.example.fuelcalculator.ui.home.HomeFragment
import com.example.fuelcalculator.ui.locate.LocateFragment
import com.example.fuelcalculator.ui.settings.SettingsActivity
import com.example.fuelcalculator.ui.vehicles.MyVehiclesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navHome: LinearLayout
    private lateinit var navLocate: LinearLayout
    private lateinit var navVehicles: LinearLayout
    private lateinit var navCalculator: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupBottomNavigation()

        // Show home fragment by default
        if (savedInstanceState == null) {
            showFragment(HomeFragment())
            updateNavigation(R.id.navHome)
        }
    }

    //Initializing different views "pages" of the app dynamically updating the UI with fragments
    private fun initializeViews() {
        navHome = findViewById(R.id.navHome)
        navLocate = findViewById(R.id.navLocate)
        navVehicles = findViewById(R.id.navVehicles)
        navCalculator = findViewById(R.id.navCalculator)
    }

    //Setting up bottom nav bar and fixing it into the screen
    private fun setupBottomNavigation() {
        navHome.setOnClickListener {
            showFragment(HomeFragment())
            updateNavigation(R.id.navHome)
        }

        navVehicles.setOnClickListener {
            showFragment(MyVehiclesFragment())
            updateNavigation(R.id.navVehicles)
        }

        navLocate.setOnClickListener {
            showFragment(LocateFragment())
            updateNavigation(R.id.navLocate)
        }

        navCalculator.setOnClickListener {
            showFragment(CalculatorFragment())
            updateNavigation(R.id.navCalculator)
        }
    }

    //Call to show fragment when certain button is clicked/swapping pages
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Method to navigate to Settings
    fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    //Updating navigation depending on where user is currently
    private fun updateNavigation(activeItemId: Int) {
        BottomNavHelper.setActiveNavItem(
            navHome, navLocate, navVehicles, navCalculator,
            activeItemId
        )
    }
}