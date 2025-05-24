package com.kashi.democalai.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kashi.democalai.data.repository.AuthRepository
import com.kashi.democalai.utils.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isInitializing: Boolean = true, // Track initial auth state determination
    val user: FirebaseUser? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Listen to auth state changes
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoading = false,
                    isInitializing = false // Auth state has been determined
                )
                
                // Set user properties for analytics
                user?.let { firebaseUser ->
                    analyticsHelper.setUserId(firebaseUser.uid)
                    analyticsHelper.setUserProperty("user_type", "authenticated")
                    firebaseUser.email?.let { email ->
                        analyticsHelper.setUserProperty("email_domain", email.substringAfter("@"))
                    }
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.signInWithGoogle(context)
                .onSuccess { user ->
                    // Log successful sign in
                    analyticsHelper.logUserSignIn("google")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
                .onFailure { exception ->
                    // Log sign in error
                    analyticsHelper.logError(
                        errorType = "auth_sign_in_failed",
                        errorMessage = exception.message ?: "Unknown error",
                        screenName = "login"
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            authRepository.signOut(context)
                .onSuccess {
                    // Log successful sign out
                    analyticsHelper.logUserSignOut()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = null,
                        error = null
                    )
                }
                .onFailure { exception ->
                    // Log sign out error
                    analyticsHelper.logError(
                        errorType = "auth_sign_out_failed",
                        errorMessage = exception.message ?: "Sign out failed",
                        screenName = "home"
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Sign out failed"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 