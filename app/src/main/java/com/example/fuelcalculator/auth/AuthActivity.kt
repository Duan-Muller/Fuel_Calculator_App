package com.example.fuelcalculator.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fuelcalculator.MainActivity
import com.example.fuelcalculator.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class AuthActivity : AppCompatActivity(), AuthContract.View {

    private lateinit var presenter: AuthContract.Presenter
    private lateinit var tabLayout: TabLayout
    private lateinit var confirmPasswordText: TextView
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var authButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var alreadyHaveAccount: TextView
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        presenter = AuthPresenter(this)
        initializeViews()
        setupTabLayout()
        setupClickListeners()
    }

    private fun initializeViews() {
        tabLayout = findViewById(R.id.tabLayout)
        confirmPasswordText = findViewById(R.id.confirmPasswordText)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        rememberMeCheckbox = findViewById(R.id.rememberCBox)
        authButton = findViewById(R.id.authBtn)
        forgotPasswordText = findViewById(R.id.forgotPwdText)
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> presenter.onLoginTabSelected()
                    1 -> presenter.onSignUpTabSelected()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        authButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (tabLayout.selectedTabPosition == 0) {
                presenter.onLoginClicked(email, password)
            } else {
                val confirmPassword = confirmPasswordInput.text.toString()
                presenter.onSignUpClicked(email, password, confirmPassword)
            }
        }

        forgotPasswordText.setOnClickListener {
            presenter.onForgotPasswordClicked()
        }
    }

    override fun showLoginView() {
        confirmPasswordText.visibility = View.GONE
        confirmPasswordLayout.visibility = View.GONE
        alreadyHaveAccount.visibility = View.GONE
        rememberMeCheckbox.visibility = View.VISIBLE
        forgotPasswordText.visibility = View.VISIBLE
        authButton.text = getString(R.string.login)
    }

    override fun showSignUpView() {
        confirmPasswordText.visibility = View.VISIBLE
        confirmPasswordLayout.visibility = View.VISIBLE
        alreadyHaveAccount.visibility = View.VISIBLE
        rememberMeCheckbox.visibility = View.GONE
        forgotPasswordText.visibility = View.GONE
        authButton.text = getString(R.string.signup)
    }

    override fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}