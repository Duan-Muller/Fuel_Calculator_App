package com.example.fuelcalculator.auth

interface AuthContract {
    interface View {
        fun showLoginView()
        fun showSignUpView()
        fun navigateToMain()
        fun showError(message: String)
    }

    interface Presenter {
        fun onLoginTabSelected()
        fun onSignUpTabSelected()
        fun onLoginClicked(email: String, password: String)
        fun onSignUpClicked(email: String, password: String, confirmPassword: String)
        fun onGoogleSignInClicked()
        fun onMetaSignInClicked()
        fun onGithubSignInClicked()
        fun onForgotPasswordClicked()
        fun onDestroy()
    }
}