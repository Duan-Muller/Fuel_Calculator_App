package com.example.fuelcalculator.ui.home

import androidx.lifecycle.ViewModel
import com.example.fuelcalculator.data.repository.FirebaseAuthManager

class HomePresenter(
    private var view: HomeContract.View?,
    private val authManager: FirebaseAuthManager
) : ViewModel(), HomeContract.Presenter {

    override fun onViewCreated() {
        //TODO: Load user expenses and refuel history
        view?.updateExpenseAmount("R800,00")// Placeholder data
    }

    override fun onTripButtonClicked() {
        view?.navigateToTrip()
    }

    override fun onReportButtonClicked() {
        view?.navigateToReport()
    }

    override fun onProfileButtonClicked() {
        view?.navigateToProfile()
    }

    override fun onSeeAllClicked() {
        view?.navigateToAllHistory()
    }

    override fun onDestroy() {
        view = null
    }


}