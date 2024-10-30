package com.example.fuelcalculator.ui.welcome

class WelcomePresenter(private var view: WelcomeContract.View?) : WelcomeContract.Presenter {

    override fun onGetStartedClicked() {
        view?.navigateToAuth()
    }

    override fun onDestroy() {
        //Clean up to prevent memory leaks
        view = null
    }
}