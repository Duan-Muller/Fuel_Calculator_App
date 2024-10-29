package com.example.fuelcalculator.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.example.fuelcalculator.data.repository.SessionManager
import com.example.fuelcalculator.ui.auth.AuthActivity
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        authManager = FirebaseAuthManager()
        sessionManager = SessionManager(this)

        initializeViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        userNameText = findViewById(R.id.userName)
        userEmailText = findViewById(R.id.userEmail)
    }

    private fun loadUserData() {
        val currentUser = authManager.getCurrentUser()
        currentUser?.let { user ->
            //Get user info from Firebase
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userNameText.text = document.getString("username") ?: "User"
                        userEmailText.text = user.email
                    }
                }
        }
    }

    private fun setupClickListeners() {
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

