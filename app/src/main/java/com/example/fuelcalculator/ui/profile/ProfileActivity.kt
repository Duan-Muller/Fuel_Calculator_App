package com.example.fuelcalculator.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.data.repository.SessionManager
import com.example.fuelcalculator.ui.auth.AuthActivity
import com.example.fuelcalculator.ui.help.HelpCenterActivity
import com.example.fuelcalculator.ui.settings.SettingsActivity
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileInitialText: TextView
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        authManager = FirebaseAuthManager()
        sessionManager = SessionManager(this)

        initializeViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        userNameText = findViewById(R.id.userName)
        userEmailText = findViewById(R.id.userEmail)
        profileInitialText = findViewById(R.id.profileInitial)
    }

    private fun loadUserData() {
        val currentUser = authManager.getCurrentUser()
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username") ?: "User"
                        userNameText.text = username
                        userEmailText.text = user.email
                        // Set the first letter of username as profile initial
                        profileInitialText.text = username.firstOrNull()?.uppercase() ?: "U"
                    }
                }
        }
    }

    private fun setupClickListeners() {
        //Edit profile button
        findViewById<LinearLayout>(R.id.editProfileButton).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        //Settings button
        findViewById<LinearLayout>(R.id.settingsButton).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        //Help Center button
        findViewById<LinearLayout>(R.id.helpCenterButton).setOnClickListener {
            val intent = Intent(this, HelpCenterActivity::class.java)
            startActivity(intent)
        }

        //Sign out button
        findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.signOutButton).setOnClickListener {
            //Sign out and clear user session
            authManager.signOut()
            sessionManager.clearSession()

            //Navigate back to login
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        //Back button
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

}

