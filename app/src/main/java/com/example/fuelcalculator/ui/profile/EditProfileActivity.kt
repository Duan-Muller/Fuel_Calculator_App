package com.example.fuelcalculator.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {
    private var isAwaitingEmailVerification = false

    private lateinit var currentUsername: TextView
    private lateinit var currentEmail: TextView
    private lateinit var profileInitial: TextView

    private lateinit var newUsernameLayout: TextInputLayout
    private lateinit var newEmailLayout: TextInputLayout
    private lateinit var confirmEmailLayout: TextInputLayout
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout

    private lateinit var newUsernameInput: TextInputEditText
    private lateinit var newEmailInput: TextInputEditText
    private lateinit var confirmEmailInput: TextInputEditText
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText

    private lateinit var updateProfileButton: MaterialButton
    private lateinit var clearFieldsButton: MaterialButton
    private lateinit var backButton: MaterialButton

    private lateinit var authManager: FirebaseAuthManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        isAwaitingEmailVerification = savedInstanceState?.getBoolean("awaiting_verification") ?: false

        authManager = FirebaseAuthManager()
        initializeViews()
        loadCurrentUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        currentUsername = findViewById(R.id.currentUsername)
        currentEmail = findViewById(R.id.currentEmail)
        profileInitial = findViewById(R.id.profileInitial)

        newUsernameLayout = findViewById(R.id.newUsernameLayout)
        newEmailLayout = findViewById(R.id.newEmailLayout)
        confirmEmailLayout = findViewById(R.id.confirmEmailLayout)
        newPasswordLayout = findViewById(R.id.newPasswordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)

        newUsernameInput = findViewById(R.id.newUsernameInput)
        newEmailInput = findViewById(R.id.newEmailInput)
        confirmEmailInput = findViewById(R.id.confirmEmailInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)

        updateProfileButton = findViewById(R.id.updateProfileButton)
        clearFieldsButton = findViewById(R.id.clearFieldsButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupClickListeners() {
        updateProfileButton.setOnClickListener {
            if (validateInputs()) {
                updateProfile()
            }
        }

        clearFieldsButton.setOnClickListener {
            clearFields()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Reset all errors
        newUsernameLayout.error = null
        newEmailLayout.error = null
        confirmEmailLayout.error = null
        newPasswordLayout.error = null
        confirmPasswordLayout.error = null

        // Validate username if provided
        val newUsername = newUsernameInput.text.toString()
        if (newUsername.isNotEmpty() && newUsername.length < 3) {
            newUsernameLayout.error = "Username must be at least 3 characters"
            isValid = false
        }

        // Validate email if provided
        val newEmail = newEmailInput.text.toString()
        val confirmEmail = confirmEmailInput.text.toString()
        if (newEmail.isNotEmpty() || confirmEmail.isNotEmpty()) {
            if (newEmail.isEmpty()) {
                newEmailLayout.error = "Please enter new email"
                isValid = false
            }
            if (confirmEmail.isEmpty()) {
                confirmEmailLayout.error = "Please confirm new email"
                isValid = false
            }
            if (newEmail != confirmEmail) {
                confirmEmailLayout.error = "Emails do not match"
                isValid = false
            }
        }

        // Validate password if provided
        val newPassword = newPasswordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()
        if (newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (newPassword.length < 6) {
                newPasswordLayout.error = "Password must be at least 6 characters"
                isValid = false
            }
            if (newPassword != confirmPassword) {
                confirmPasswordLayout.error = "Passwords do not match"
                isValid = false
            }
        }

        return isValid
    }

    private fun updateProfile() {
        val currentUser = authManager.getCurrentUser() ?: return

        // Update username if provided
        val newUsername = newUsernameInput.text.toString()
        if (newUsername.isNotEmpty()) {
            db.collection("users").document(currentUser.uid)
                .update("username", newUsername)
                .addOnSuccessListener {
                    Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
                    loadCurrentUserData()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show()
                }
        }

        // Update email if provided
        val newEmail = newEmailInput.text.toString()
        val confirmEmail = confirmEmailInput.text.toString()
        if (newEmail.isNotEmpty() && newEmail == confirmEmail) {
            updateEmail(currentUser, newEmail)
        }

        // Update password if provided
        val newPassword = newPasswordInput.text.toString()
        if (newPassword.isNotEmpty()) {
            updatePassword(currentUser, newPassword)
        }
    }

    private fun updateEmail(currentUser: FirebaseUser, newEmail: String) {
        if (newEmail == currentUser.email) {
            Toast.makeText(this, "New email is same as current email", Toast.LENGTH_SHORT).show()
            return
        }

        showPasswordVerificationDialog { password ->
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show()
                return@showPasswordVerificationDialog
            }

            updateProfileButton.isEnabled = false
            updateProfileButton.text = "Updating..."

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Step 1: Check if email exists in Firestore
                    val emailExists = withContext(Dispatchers.IO) {
                        val querySnapshot = db.collection("users")
                            .whereEqualTo("email", newEmail)
                            .get()
                            .await()
                        !querySnapshot.isEmpty
                    }

                    if (emailExists) {
                        Toast.makeText(this@EditProfileActivity,
                            "This email is already registered",
                            Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Step 2: Reauthenticate
                    withContext(Dispatchers.IO) {
                        val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
                        currentUser.reauthenticate(credential).await()
                    }

                    // Step 3: Send verification email and update email
                    withContext(Dispatchers.IO) {
                        currentUser.verifyBeforeUpdateEmail(newEmail).await()
                        isAwaitingEmailVerification = true // Set the flag
                    }

                    Toast.makeText(this@EditProfileActivity,
                        "Verification email sent. Please check your email to complete the change.",
                        Toast.LENGTH_LONG).show()

                    clearFields()

                } catch (e: Exception) {
                    val errorMessage = when {
                        e.message?.contains("password is invalid", ignoreCase = true) == true ->
                            "Incorrect password"
                        e.message?.contains("email already in use", ignoreCase = true) == true ->
                            "This email is already registered"
                        e.message?.contains("invalid email", ignoreCase = true) == true ->
                            "Invalid email format"
                        e.message?.contains("network error", ignoreCase = true) == true ->
                            "Network error. Please check your connection"
                        e.message?.contains("requires recent authentication", ignoreCase = true) == true ->
                            "Please try again with your current password"
                        else -> "Failed to update email: ${e.message}"
                    }
                    Toast.makeText(this@EditProfileActivity, errorMessage, Toast.LENGTH_LONG).show()
                } finally {
                    updateProfileButton.isEnabled = true
                    updateProfileButton.text = "Update Profile"
                }
            }
        }
    }

    // Add this function to handle the email verification completion
    override fun onResume() {
        super.onResume()

        // Only check for email verification if we're expecting it
        if (isAwaitingEmailVerification) {
            val currentUser = authManager.getCurrentUser()
            if (currentUser != null && currentUser.email != currentEmail.text.toString()) {
                // Email has been verified and changed, now update Firestore
                db.collection("users")
                    .document(currentUser.uid)
                    .update("email", currentUser.email)
                    .addOnSuccessListener {
                        Toast.makeText(this,
                            "Email update completed successfully",
                            Toast.LENGTH_SHORT).show()
                        loadCurrentUserData()
                        isAwaitingEmailVerification = false // Reset the flag
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this,
                            "Failed to update email in database: ${e.message}",
                            Toast.LENGTH_LONG).show()
                        isAwaitingEmailVerification = false // Reset the flag
                    }
            }
        }
    }

    // Add this to handle activity destruction
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("awaiting_verification", isAwaitingEmailVerification)
    }

    // Optional: Update loadCurrentUserData() to use coroutines as well
    private fun loadCurrentUserData() {
        val currentUser = authManager.getCurrentUser() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val document = withContext(Dispatchers.IO) {
                    db.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()
                }

                if (document != null) {
                    val username = document.getString("username") ?: "User"
                    currentUsername.text = username
                    currentEmail.text = currentUser.email
                    profileInitial.text = username.firstOrNull()?.uppercase() ?: "U"
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAuthError(e: Exception) {
        val errorMessage = when {
            e.message?.contains("password is invalid", ignoreCase = true) == true ->
                "Incorrect password"
            e.message?.contains("network error", ignoreCase = true) == true ->
                "Network error. Please check your connection"
            e.message?.contains("requires recent authentication", ignoreCase = true) == true ->
                "Please try again with your current password"
            else -> "Authentication failed: ${e.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun handleUpdateError(e: Exception) {
        val errorMessage = when {
            e.message?.contains("email already in use", ignoreCase = true) == true ->
                "This email is already registered"
            e.message?.contains("invalid email", ignoreCase = true) == true ->
                "Invalid email format"
            e.message?.contains("network error", ignoreCase = true) == true ->
                "Network error. Please check your connection"
            else -> "Failed to update email: ${e.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun updatePassword(currentUser: FirebaseUser, newPassword: String) {
        showPasswordVerificationDialog { currentPassword ->
            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show()
                return@showPasswordVerificationDialog
            }

            updateProfileButton.isEnabled = false
            updateProfileButton.text = "Updating..."

            try {
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential)
                    .addOnSuccessListener {
                        currentUser.updatePassword(newPassword)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                clearFields()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update password: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        val errorMessage = when {
                            e.message?.contains("password is invalid", ignoreCase = true) == true ->
                                "Incorrect password"
                            else -> "Authentication failed: ${e.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener {
                        updateProfileButton.isEnabled = true
                        updateProfileButton.text = "Update Profile"
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                updateProfileButton.isEnabled = true
                updateProfileButton.text = "Update Profile"
            }
        }
    }

    private fun showPasswordVerificationDialog(onPasswordVerified: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter your current password"
            setPadding(50, 50, 50, 50)
        }

        builder.setTitle("Verify Password")
            .setMessage("Please enter your current password to continue")
            .setView(input)
            .setPositiveButton("Verify", null) // Set null initially
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = builder.create()

        // Override the positive button's onClick after dialog creation
        dialog.setOnShowListener { dialogInterface ->
            val button = (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = false // Initially disabled

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    button.isEnabled = !s.isNullOrEmpty()
                }
            })

            button.setOnClickListener {
                val password = input.text.toString()
                if (password.isNotEmpty()) {
                    onPasswordVerified(password)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun clearFields() {
        newUsernameInput.text?.clear()
        newEmailInput.text?.clear()
        confirmEmailInput.text?.clear()
        newPasswordInput.text?.clear()
        confirmPasswordInput.text?.clear()

        newUsernameLayout.error = null
        newEmailLayout.error = null
        confirmEmailLayout.error = null
        newPasswordLayout.error = null
        confirmPasswordLayout.error = null
    }
}