package com.example.fuelcalculator.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.data.repository.SessionManager
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch

class AuthPresenter(
    private var view: AuthContract.View?,
    private val authManager: FirebaseAuthManager,
    private val sessionManager: SessionManager
) : ViewModel(), AuthContract.Presenter {

    override fun onLoginTabSelected() {
        view?.showLoginView()
    }

    override fun onSignUpTabSelected() {
        view?.showSignUpView()
    }

    init {
        // Delay checking for remembered email
        checkRememberedUser()
    }

    private fun checkRememberedUser() {
        if (sessionManager.isRememberMeEnabled()) {
            sessionManager.getSavedUsername()?.let { username ->
                view?.setUsername(username)
                view?.setRememberMe(true)
            }
        }
    }

    override fun onLoginClicked(username: String, password: String) {
        //Login logic
        if (!validateLoginInputs(username, password)) return

        view?.showLoading()

        viewModelScope.launch {
           authManager.signIn(username, password).fold(
               onSuccess = { user ->
                   //Save session if remember me is checked
                   val rememberMe = view?.isRememberMeChecked() ?: false
                   sessionManager.createLoginSession(username, rememberMe)

                   view?.hideLoading()
                   view?.navigateToHome()
               },
               onFailure = { exception ->
                   view?.hideLoading()
                   when(exception.message) {
                       "User not found" ->
                           view?.showError("User not found")
                       "Invalid user data" ->
                           view?.showError("Account error. Contact support")
                       else -> view?.showError("Login failed: ${exception.message}")
                   }
               }
           )

        }
    }

    override fun onSignUpClicked(username: String, email: String, password: String, confirmPassword: String) {
        //First check if password fields match
        if (password != confirmPassword) {
            view?.showError("Passwords do not match")
            return
        }

        //Trim inputs
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()

        //Validating sign up inputs
        if (!validateSignupInputs(trimmedUsername, trimmedEmail, password)) return

        view?.showLoading()

        viewModelScope.launch {
            authManager.signUp(username, email, password).fold(
                onSuccess = { user ->
                    view?.hideLoading()
                    view?.showSuccess("Account created successfully")
                    view?.showLoginView()
                    view?.clearInputs()
                },
                onFailure = { exception ->
                    view?.hideLoading()
                    when(exception.message) {
                        "Username already taken" ->
                            view?.showError("Username is already taken")
                        else -> handleFirebaseAuthError(exception)
                    }

                }
            )
        }
    }

    //Email Validation
    private fun isValidEmail(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    //Password Validation
    private fun isValidPassword(password: String): Boolean{
        //Password is at least 8 characters
        if (password.length < 8) return false

        //Password contains at least one digit
        if (!password.any {it.isDigit()}) return false

        //Password contains at least one letter
        if (!password.any {it.isLetter()}) return false

        //Contains at least one special character
        val specialChars = "!@#$%^&()_-+={[}]|:;'<,>.?/"
        if (!password.any { specialChars.contains(it) }) return false

        return true
    }


    //Validate Login Credentials
    private fun validateLoginInputs(username: String, password: String): Boolean {
        when {
            username.isEmpty() -> {
                view?.showError("Username cannot be empty")
                return false
            }
            password.isEmpty() -> {
                view?.showError("Password cannot be empty")
                return false
            }
        }
        return true
    }


    //Validate Sign up Credentials
    private fun validateSignupInputs(username: String, email: String, password: String): Boolean{
        when{
            username.isEmpty() -> {
                view?.showError("Username cannot be empty")
                return false
            }

            username.length < 4 -> {
                view?.showError("Username must be at least 4 characters long")
                return false
            }

            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> {
                view?.showError("Username can only contain letters, numbers and underscores")
                return false
            }

            email.isEmpty() -> {
                view?.showError("Email cannot be empty")
                return false
            }

            !isValidEmail(email) -> {
                view?.showError("Please enter a valid email")
                return false
            }

            password.isEmpty() -> {
                view?.showError("Password cannot be empty")
                return false
            }
            !isValidPassword(password) -> {
                view?.showError("""
                    Password must:
                    -  Be at least 8 characters long
                    -  Contain at least one digit
                    -  Contain at least one letter
                    -  Contain at least one special character
                """.trimIndent())
                return false
            }
        }
        return true
    }

    private fun handleFirebaseAuthError(exception: Throwable) {
        when(exception) {
            is FirebaseAuthWeakPasswordException -> {
                view?.showError("Password is too weak")
            }
            is FirebaseAuthInvalidCredentialsException -> {
                view?.showError("Invalid email or password")
            }
            is FirebaseAuthUserCollisionException -> {
                view?.showError("Email already in use")
            }
            else -> {
                view?.showError("Login failed: ${exception.message}")
            }
        }
    }

    override fun onGoogleSignInClicked() {
        // Maybe implement Google Sign In
        view?.showError("Google Sign In coming soon")
    }

    override fun onMetaSignInClicked() {
        // Maybe implement Meta Sign In
        view?.showError("Meta Sign In coming soon")
    }

    override fun onGithubSignInClicked() {
        // Maybe implement Github Sign In
        view?.showError("Github Sign In coming soon")
    }

    override fun onForgotPasswordClicked() {
        val email = view?.getCurrentEmail() ?: ""
        if (email.isEmpty() || !isValidEmail(email)) {
            view?.showError("Please enter a valid email")
            return
        }
        view?.showLoading()
        viewModelScope.launch {
            try {
                authManager.sendPasswordResetEmail(email).fold(
                    onSuccess = {
                        view?.hideLoading()
                        view?.showSuccess("Password reset email sent")
                    },
                    onFailure = { exception ->
                        view?.hideLoading()
                        view?.showError("Failed to send password reset email: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("Failed to send password reset email: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        view = null
    }
}