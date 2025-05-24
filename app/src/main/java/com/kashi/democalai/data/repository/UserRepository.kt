package com.kashi.democalai.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kashi.democalai.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    companion object {
        private const val TAG = "UserRepository"
    }

    suspend fun createOrUpdateUser(firebaseUser: FirebaseUser): Result<User> {
        return try {
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Unknown User",
                profileImageUrl = firebaseUser.photoUrl?.toString()
            )

            // Use merge to update only provided fields and preserve existing data
            usersCollection.document(firebaseUser.uid)
                .set(user, SetOptions.merge())
                .await()

            Log.d(TAG, "User document created/updated successfully for ${firebaseUser.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create/update user document", e)
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            Log.d(TAG, "Retrieved user: $user")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user document", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        displayName: String? = null,
        bio: String? = null,
        profileImageUrl: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            displayName?.let { updates["displayName"] = it }
            bio?.let { updates["bio"] = it }
            profileImageUrl?.let { updates["profileImageUrl"] = it }

            if (updates.isNotEmpty()) {
                usersCollection.document(userId)
                    .update(updates)
                    .await()
                Log.d(TAG, "User profile updated successfully")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user profile", e)
            Result.failure(e)
        }
    }

    suspend fun incrementPostCount(userId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val userRef = usersCollection.document(userId)
                val snapshot = transaction.get(userRef)
                val currentCount = snapshot.getLong("postCount") ?: 0
                transaction.update(userRef, "postCount", currentCount + 1)
            }.await()
            Log.d(TAG, "Post count incremented for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to increment post count", e)
            Result.failure(e)
        }
    }
} 