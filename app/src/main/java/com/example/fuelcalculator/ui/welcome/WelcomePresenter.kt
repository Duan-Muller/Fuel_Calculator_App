package com.example.fuelcalculator.ui.welcome

class WelcomePresenter(private var view: WelcomeContract.View?) : WelcomeContract.Presenter {

    override fun onGetStartedClicked() {
        //TODO Handle the get started button click
        view?.navigateToAuth()
    }

    override fun onDestroy() {
        //Clean up to prevent memory leaks
        view = null
    }
}