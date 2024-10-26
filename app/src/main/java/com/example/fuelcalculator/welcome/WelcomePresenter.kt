package com.example.fuelcalculator.welcome

class WelcomePresenter(private var view: WelcomeContract.View?) : WelcomeContract.Presenter {

    override fun onGetStartedClicked() {
        //Handle the get started button click
        view?.navigateToMain()
    }

    override fun onDestroy() {
        //Clean up to prevent memory leaks
        view = null
    }
}