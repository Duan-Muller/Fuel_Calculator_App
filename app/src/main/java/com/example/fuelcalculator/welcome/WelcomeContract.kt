package com.example.fuelcalculator.welcome

interface WelcomeContract {
    interface View {
        fun navigateToMain()
    }

    interface Presenter {
        fun onGetStartedClicked()
        fun onDestroy()
    }
}