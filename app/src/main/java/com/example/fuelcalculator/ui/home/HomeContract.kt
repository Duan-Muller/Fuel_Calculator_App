package com.example.fuelcalculator.ui.home

import com.example.fuelcalculator.data.model.RefuelRecord

interface HomeContract {

    interface View {

        //Navigation methods
        fun navigateToTrip()
        fun navigateToReport()
        fun navigateToProfile()
        fun navigateToAllHistory()

        //User feedback methods
        fun showError(message: String)

        //Loading state methods
        fun showLoading()
        fun hideLoading()

        //Utility methods
        fun updateExpenseAmount(amount: String)
        fun showRefuelHistory(history: List<RefuelRecord>)

    }

    interface Presenter {

        fun onViewCreated()
        fun onTripButtonClicked()
        fun onReportButtonClicked()
        fun onProfileButtonClicked()
        fun onSeeAllClicked()
        fun onDestroy()

    }

}