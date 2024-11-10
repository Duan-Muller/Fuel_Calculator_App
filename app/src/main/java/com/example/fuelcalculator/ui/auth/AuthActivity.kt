package com.example.fuelcalculator.ui.auth

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.data.repository.SessionManager
import com.example.fuelcalculator.ui.home.HomeFragment
import com.example.fuelcalculator.ui.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity(), AuthContract.View {

    //Finding elements on UI
    private lateinit var emailInputText: TextView
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var usernameText: TextView
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var usernameInput: TextInputEditText
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var sessionManager: SessionManager
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
    private lateinit var progressBar: ProgressBar
    private lateinit var googleLoginButton: View
    private lateinit var facebookLoginButton: View
    private lateinit var githubLoginButton: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        authManager = FirebaseAuthManager()

        //Check if use is logged in already
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, HomeFragment::class.java))
            finish()
            return
        }

        //Initialize if user not logged in
        setContentView(R.layout.activity_auth)
        initializeViews()
        presenter = AuthPresenter(this, authManager, sessionManager)
        setupTabLayout()
        setupClickListeners()
    }

    override fun setEmail(email: String) {
        emailInput.setText(email)
    }

    override fun setRememberMe(rememberMe: Boolean) {
        rememberMeCheckbox.isChecked = rememberMe
    }

    override fun isRememberMeChecked(): Boolean {
        return rememberMeCheckbox.isChecked
    }

    override fun setUsername(username: String) {
        usernameInput.setText(username)
    }

    private fun initializeViews() {
        emailInputText = findViewById(R.id.emailInputText)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        usernameText = findViewById(R.id.usernameText)
        usernameLayout = findViewById(R.id.usernameLayout)
        usernameInput = findViewById(R.id.usernameInput)
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
        progressBar = findViewById(R.id.progressBar)
        googleLoginButton = findViewById(R.id.googleLoginButton)
        facebookLoginButton = findViewById(R.id.facebookLoginButton)
        githubLoginButton = findViewById(R.id.githubLoginButton)
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
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (tabLayout.selectedTabPosition == 0) {
                //Login
                presenter.onLoginClicked(username, password)
            } else {
                //Sign up
                val email = emailInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()
                presenter.onSignUpClicked(username, email , password, confirmPassword)
            }
        }

        forgotPasswordText.setOnClickListener {
            presenter.onForgotPasswordClicked()
        }

        // Social media login redirects
        val comingSoonMessage = "Full integration coming soon! Redirecting to login page..."

        googleLoginButton.setOnClickListener {
            Toast.makeText(this, comingSoonMessage, Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/signin"))
            startActivity(intent)
        }

        facebookLoginButton.setOnClickListener {
            Toast.makeText(this, comingSoonMessage, Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/login"))
            startActivity(intent)
        }

        githubLoginButton.setOnClickListener {
            Toast.makeText(this, comingSoonMessage, Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login"))
            startActivity(intent)
        }
    }

    override fun showLoginView() {
        tabLayout.getTabAt(0)?.select()
        emailInputText.visibility = View.GONE
        emailInputLayout.visibility = View.GONE
        confirmPasswordText.visibility = View.GONE
        confirmPasswordLayout.visibility = View.GONE
        alreadyHaveAccount.visibility = View.GONE
        rememberMeCheckbox.visibility = View.VISIBLE
        forgotPasswordText.visibility = View.VISIBLE
        authButton.text = getString(R.string.login)
    }

    override fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.emailInput)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Reset", null) // Set to null initially
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Set click listener after dialog is shown to prevent automatic dismissal
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val email = emailInput.text.toString()
            if (email.isEmpty()) {
                showError("Please enter your email")
                return@setOnClickListener
            }

            showLoading()
            lifecycleScope.launch {
                try {
                    authManager.sendPasswordResetEmail(email).fold(
                        onSuccess = {
                            hideLoading()
                            showSuccess("Password reset email sent")
                            dialog.dismiss()
                        },
                        onFailure = { exception ->
                            hideLoading()
                            showError("Failed to send password reset email: ${exception.message}")
                        }
                    )
                } catch (e: Exception) {
                    hideLoading()
                    showError("Failed to send password reset email: ${e.message}")
                }
            }
        }
    }

    override fun showSignUpView() {
        emailInputText.visibility = View.VISIBLE
        emailInputLayout.visibility = View.VISIBLE
        confirmPasswordText.visibility = View.VISIBLE
        confirmPasswordLayout.visibility = View.VISIBLE
        alreadyHaveAccount.visibility = View.VISIBLE
        rememberMeCheckbox.visibility = View.GONE
        forgotPasswordText.visibility = View.GONE
        authButton.text = getString(R.string.signup)
    }

    override fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun clearInputs() {
        usernameInput.text?.clear()
        emailInput.text?.clear()
        passwordInput.text?.clear()
        confirmPasswordInput.text?.clear()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        presenter?.onDestroy()
        super.onDestroy()
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showSuccess(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    override fun getCurrentEmail(): String {
        return emailInput.text.toString()
    }
}