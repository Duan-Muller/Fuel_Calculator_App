package com.example.fuelcalculator.ui.home

import androidx.lifecycle.ViewModel
import com.example.fuelcalculator.data.db.RefuelDBHelper
import com.example.fuelcalculator.data.repository.FirebaseAuthManager

class HomePresenter(
    private var view: HomeContract.View?,
    private val authManager: FirebaseAuthManager,
    private val refuelDBHelper: RefuelDBHelper
) : ViewModel(), HomeContract.Presenter {

    override fun onViewCreated() {
        updateExpenseAmount()
    }

    private fun updateExpenseAmount() {
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId != null) {
            val total = refuelDBHelper.getCurrentMonthTotal(currentUserId)
            view?.updateExpenseAmount("R${String.format("%.2f", total)}")
        } else {
            view?.updateExpenseAmount("R0.00")
        }
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