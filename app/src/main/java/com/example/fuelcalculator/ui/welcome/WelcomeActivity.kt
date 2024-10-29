package com.example.fuelcalculator.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.airbnb.lottie.LottieAnimationView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.repository.SessionManager
import com.example.fuelcalculator.ui.auth.AuthActivity
import com.example.fuelcalculator.ui.home.HomeActivity

class WelcomeActivity : AppCompatActivity(), WelcomeContract.View {

    private var presenter: WelcomeContract.Presenter? = null
    private lateinit var sessionManager: SessionManager
    private val TAG = "WelcomeActivity" // For logging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "Starting onCreate")

            //Initialize session and check login status
            sessionManager = SessionManager(this)
            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                return
            }

            setContentView(R.layout.activity_welcome)

            val carAnimation = findViewById<LottieAnimationView>(R.id.carAnimation)

            // Use local animation
            carAnimation.setAnimation(R.raw.car_animation)
            carAnimation.repeatCount = -1  // Infinite loop
            carAnimation.speed = 1f        // Normal speed
            carAnimation.playAnimation()

            // Initialize presenter
            presenter = WelcomePresenter(this)

            // Set click listener
            val startButton = findViewById<Button>(R.id.startBtn)
            Log.d(TAG, "Found button: ${startButton != null}")

            startButton.setOnClickListener {
                Log.d(TAG, "Button clicked")
                presenter?.onGetStartedClicked()
            }

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    override fun navigateToAuth() {
        try {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to auth", e)
        }
    }

    override fun onDestroy() {
        try {
            findViewById<LottieAnimationView>(R.id.carAnimation)?.cancelAnimation()
            presenter?.onDestroy()  // Safe call on nullable presenter
            presenter = null        // Clean up reference
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
            super.onDestroy()
        }
    }
}