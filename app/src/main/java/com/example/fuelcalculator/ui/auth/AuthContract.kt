package com.example.fuelcalculator.ui.auth

interface AuthContract {
    interface View {
        //Navigation methods
        fun showLoginView()
        fun showSignUpView()
        fun navigateToHome()

        //User feedback methods
        fun showError(message: String)
        fun showSuccess(message: String)

        //Loading state methods
        fun showLoading()
        fun hideLoading()

        //Utility methods
        fun getCurrentEmail(): String
        fun clearInputs()
        fun setEmail(email: String)
        fun setRememberMe(rememberMe: Boolean)
        fun isRememberMeChecked(): Boolean
        fun setUsername(username: String)
        fun showForgotPasswordDialog()
    }

    interface Presenter {
        fun onLoginTabSelected()
        fun onSignUpTabSelected()
        fun onLoginClicked(username: String, password: String)
        fun onSignUpClicked(username: String, email: String, password: String, confirmPassword: String)
        fun onGoogleSignInClicked()
        fun onMetaSignInClicked()
        fun onGithubSignInClicked()
        fun onForgotPasswordClicked()
        fun onDestroy()
    }
}