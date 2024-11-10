package com.example.fuelcalculator.ui.help

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fuelcalculator.R
import com.example.fuelcalculator.data.repository.FirebaseAuthManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

class HelpCenterActivity : AppCompatActivity() {
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var userInitial: TextView
    private lateinit var authManager: FirebaseAuthManager

    // Map of help content items and their descriptions
    private val helpContent = mapOf(
        R.id.applicationDataPrivacy to HelpContent(
            "Application Data Privacy",
            "We take your privacy seriously. All data collected by the app is encrypted and stored securely on " +
                    "our servers. We follow industry-standard practices to ensure your information is protected."
        ),
        R.id.personalDataProtection to HelpContent(
            "Personal Data Protection",
            "Your personal information is protected by advanced security measures. We never share your " +
                    "personal data with third parties without your explicit consent."
        ),
        R.id.systemSecurity to HelpContent(
            "System Information Security",
            "Our app employs multiple layers of security to protect your data. This includes secure " +
                    "authentication, encrypted data transmission, and regular security audits."
        ),
        R.id.manageData to HelpContent(
            "Manage Your Data",
            "You have full control over your data. You can view, download, or delete your data at any time " +
                    "through the app settings. Data deletion requests are processed within 30 days."
        ),
        R.id.fuelData to HelpContent(
            "Your Fuel Data",
            "We collect fuel consumption data to provide you with insights and tracking capabilities. " +
                    "This includes refuel history, fuel efficiency calculations, and cost analysis."
        ),
        R.id.locationData to HelpContent(
            "Location Data",
            "Location data is only used to help you find nearby fuel stations and track your refueling " +
                    "locations. You can disable location services at any time through your device settings."
        ),
        R.id.demographicInfo to HelpContent(
            "Demographic Information",
            "Basic demographic information helps us improve our services and provide better " +
                    "recommendations. This information is anonymized and never used to identify you personally."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_center)

        authManager = FirebaseAuthManager()
        initializeViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        userNameText = findViewById(R.id.userName)
        userEmailText = findViewById(R.id.userEmail)
        userInitial = findViewById(R.id.userInitial)
    }

    private fun loadUserData() {
        val currentUser = authManager.getCurrentUser()
        currentUser?.let { user ->
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "User"
                    userNameText.text = username
                    userEmailText.text = user.email
                    userInitial.text = username.firstOrNull()?.toString()?.uppercase() ?: "U"
                }
        }
    }

    private fun setupClickListeners() {
        // Setup click listeners for each help item
        helpContent.forEach { (viewId, content) ->
            findViewById<View>(viewId)?.setOnClickListener {
                showHelpDialog(content)
            }
        }

        // Back button click listener
        findViewById<MaterialButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun showHelpDialog(content: HelpContent) {
        MaterialAlertDialogBuilder(this)
            .setTitle(content.title)
            .setMessage(content.description)
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    data class HelpContent(
        val title: String,
        val description: String
    )
}