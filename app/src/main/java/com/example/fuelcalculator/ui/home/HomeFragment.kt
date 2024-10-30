package com.example.fuelcalculator.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.model.RefuelRecord
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.ui.main.MainActivity
import com.google.android.material.button.MaterialButton

class HomeFragment :  Fragment(), HomeContract.View {

    //Presenter and Firebase Auth initialization
    private lateinit var presenter: HomeContract.Presenter
    private lateinit var authManager: FirebaseAuthManager

    //Views
    private lateinit var btnProfile: MaterialButton
    private lateinit var tvExpenseAmount: TextView
    private lateinit var btnTrip: MaterialButton
    private lateinit var btnReport: MaterialButton
    private lateinit var rvRefuelHistory : RecyclerView
    private lateinit var tvSeeAll : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = FirebaseAuthManager()
        presenter = HomePresenter(this, authManager)

        initializeViews(view)
        setupClickListeners()
        presenter.onViewCreated()
    }

    private fun initializeViews(view: View) {
        //Initialize page elements
        btnProfile = view.findViewById(R.id.btnProfile)
        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount)
        btnTrip = view.findViewById(R.id.btnTrip)
        btnReport = view.findViewById(R.id.btnReport)
        rvRefuelHistory = view.findViewById(R.id.rvRefuelHistory)
        tvSeeAll = view.findViewById(R.id.tvSeeAll)
    }

    //Click listeners for buttons
    private fun setupClickListeners() {
        btnProfile.setOnClickListener {
            presenter.onProfileButtonClicked()
        }
        btnTrip.setOnClickListener {
            presenter.onTripButtonClicked()
        }
        btnReport.setOnClickListener {
            presenter.onReportButtonClicked()
        }
        tvSeeAll.setOnClickListener {
            presenter.onSeeAllClicked()
        }
    }

    override fun navigateToTrip() {
        TODO("Implement Navigation")
    }

    override fun navigateToReport() {
        TODO("Implement Navigation")
    }

    override fun navigateToProfile() {
        (activity as? MainActivity)?.navigateToSettings()
    }

    override fun navigateToAllHistory() {
        TODO("Implement Navigation")
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        TODO("Not yet implemented")
    }

    override fun hideLoading() {
        TODO("Not yet implemented")
    }

    override fun updateExpenseAmount(amount: String) {
        tvExpenseAmount.text = amount
    }

    override fun showRefuelHistory(history: List<RefuelRecord>) {
        TODO("Not yet implemented")
    }

}