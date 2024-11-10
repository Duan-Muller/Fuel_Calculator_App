package com.example.fuelcalculator.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signUp(username: String, email: String, password: String): Result<FirebaseUser> = withContext(
        Dispatchers.IO) {
        try {
            //Create account with email and password
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()

            // Only proceed to Firestore if creation was successful
            authResult.user?.let { user ->
                try {
                    val userData = hashMapOf(
                        "username" to username.trim(),
                        "email" to email.trim(),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(user.uid).set(userData).await()
                } catch (e: Exception) {
                    println("Firestore error: ${e.message}")
                }
            }
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(username: String, password: String): Result<FirebaseUser> = withContext(
        Dispatchers.IO){
            try {

                //Find email associated with username
                val userQuery = db.collection("users")
                    .whereEqualTo("username", username.trim())
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                if (userQuery == null) {
                    return@withContext Result.failure(Exception("User not found"))
                }

                val email = userQuery.getString("email")?:
                    return@withContext Result.failure(Exception("Invalid user data"))

                //Sign in with email and password (Authentication by Firebase)
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Result.success(result.user!!)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO){
        try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun requireCurrentUserId(): String {
        return getCurrentUserId() ?: throw IllegalStateException("No authenticated user found")
    }

    suspend fun getUserDisplayName(userId: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val username = userDoc.getString("username")
            return@withContext Result.success(username)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

}