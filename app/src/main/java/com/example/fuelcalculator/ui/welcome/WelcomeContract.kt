package com.example.fuelcalculator.ui.welcome

interface WelcomeContract {
    interface View {
        fun navigateToAuth()
    }

    interface Presenter {
        fun onGetStartedClicked()
        fun onDestroy()
    }
}