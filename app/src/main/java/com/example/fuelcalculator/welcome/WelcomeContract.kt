package com.example.fuelcalculator.welcome

interface WelcomeContract {
    interface View {
        fun navigateToMain()
        // Add other view methods if needed
    }

    interface Presenter {
        fun onGetStartedClicked()
        fun onDestroy()
        // Add other presenter methods if needed
    }
}