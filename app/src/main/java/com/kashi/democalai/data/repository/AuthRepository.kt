package com.kashi.democalai.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kashi.democalai.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userRepository: UserRepository
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
        private const val WEB_CLIENT_ID = "670674373344-gnrlueqnauqf7mrirc1emh9iekk6sfgp.apps.googleusercontent.com"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val authState: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.map { firebaseUser ->
        firebaseUser?.let { 
            userRepository.getUser(it.uid).getOrNull()
        }
    }

    suspend fun signInWithGoogle(context: Context): Result<User?> {
        return try {
            val credential = getGoogleCredential(context)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            
            val result = auth.signInWithCredential(authCredential).await()
            Log.d(TAG, "signInWithCredential:success")
            
            // Create or update user document in Firestore
            result.user?.let { firebaseUser ->
                userRepository.createOrUpdateUser(firebaseUser)
                    .onSuccess { user ->
                        Log.d(TAG, "User document created/updated successfully")
                        return Result.success(user)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to create user document", e)
                        return Result.failure(e)
                    }
            }
            
            Result.success(null)
        } catch (e: Exception) {
            Log.w(TAG, "signInWithCredential:failure", e)
            Result.failure(e)
        }
    }

    private suspend fun getGoogleCredential(context: Context): CustomCredential {
        val credentialManager = CredentialManager.create(context)
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        return handleSignIn(result)
    }

    private fun handleSignIn(result: GetCredentialResponse): CustomCredential {
        val credential = result.credential
        
        if (credential is CustomCredential && 
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return credential
        } else {
            throw IllegalArgumentException("Credential is not of type Google ID!")
        }
    }

    suspend fun signOut(context: Context): Result<Unit> {
        return try {
            auth.signOut()
            
            // Clear credential state as recommended by Firebase documentation
            val credentialManager = CredentialManager.create(context)
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            
            Log.d(TAG, "User signed out successfully")
            Result.success(Unit)
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
            Result.failure(e)
        }
    }
} 