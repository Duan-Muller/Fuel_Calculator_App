package com.example.fuelcalculator.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.model.RefuelRecord
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.ui.settings.SettingsActivity
import com.google.android.material.button.MaterialButton

class HomeActivity :  AppCompatActivity(), HomeContract.View {

    private lateinit var presenter: HomeContract.Presenter
    private lateinit var authManager: FirebaseAuthManager

    //Views
    private lateinit var btnProfile: MaterialButton
    private lateinit var tvExpenseAmount: TextView
    private lateinit var btnTrip: MaterialButton
    private lateinit var btnReport: MaterialButton
    private lateinit var rvRefuelHistory : RecyclerView
    private lateinit var tvSeeAll : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        authManager = FirebaseAuthManager()
        presenter = HomePresenter(this, authManager)

        initializeViews()
        setupClickListeners()
        presenter.onViewCreated()
    }

    private fun initializeViews() {
        btnProfile = findViewById(R.id.btnProfile)
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount)
        btnTrip = findViewById(R.id.btnTrip)
        btnReport = findViewById(R.id.btnReport)
        rvRefuelHistory = findViewById(R.id.rvRefuelHistory)
        tvSeeAll = findViewById(R.id.tvSeeAll)
    }

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
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun navigateToAllHistory() {
        TODO("Implement Navigation")
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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