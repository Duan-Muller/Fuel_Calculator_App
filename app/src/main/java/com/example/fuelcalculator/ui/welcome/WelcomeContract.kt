package com.example.fuelcalculator.ui.welcome

interface WelcomeContract {
    interface View {
        fun navigateToMain()
    }

    interface Presenter {
        fun onGetStartedClicked()
        fun onDestroy()
    }
}