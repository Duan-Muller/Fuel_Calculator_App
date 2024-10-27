package com.example.fuelcalculator.auth

class AuthPresenter(private var view: AuthContract.View?) : AuthContract.Presenter {
    override fun onLoginTabSelected() {
        view?.showLoginView()
    }

    override fun onSignUpTabSelected() {
        view?.showSignUpView()
    }

    override fun onLoginClicked(email: String, password: String) {
        // Add login logic here
    }

    override fun onSignUpClicked(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            view?.showError("Passwords do not match")
            return
        }
        // Add signup logic here
    }

    override fun onGoogleSignInClicked() {
        // Maybe implement Google Sign In
    }

    override fun onMetaSignInClicked() {
        // Maybe implement Meta Sign In
    }

    override fun onGithubSignInClicked() {
        // Maybe implement Github Sign In
    }

    override fun onForgotPasswordClicked() {
        // Maybe implement Forgot Password
    }

    override fun onDestroy() {
        view = null
    }
}