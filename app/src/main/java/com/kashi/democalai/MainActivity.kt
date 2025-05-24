package com.kashi.democalai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kashi.democalai.presentation.screen.HomeScreen
import com.kashi.democalai.presentation.screen.LoginScreen
import com.kashi.democalai.presentation.screen.SplashScreen
import com.kashi.democalai.presentation.viewmodel.AuthViewModel
import com.kashi.democalai.ui.theme.MyApplicationTheme
import com.kashi.democalai.utils.AnalyticsHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Analytics
        analyticsHelper.initialize(this)
        analyticsHelper.logAppLaunch()
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SocialFeedApp(analyticsHelper)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        analyticsHelper.logAppBackground()
    }
    
    override fun onResume() {
        super.onResume()
        analyticsHelper.logDailyActiveUser()
    }
}

@Composable
fun SocialFeedApp(analyticsHelper: AnalyticsHelper) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    
    // Show splash screen while determining auth state
    if (uiState.isInitializing) {
        analyticsHelper.logScreenView("splash", "SplashScreen")
        SplashScreen()
        return
    }
    
    // Determine start destination based on auth state
    val startDestination = if (uiState.user != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable("login") {
            analyticsHelper.logScreenView("login", "LoginScreen")
            LoginScreen(
                viewModel = authViewModel,
                analyticsHelper = analyticsHelper,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            analyticsHelper.logScreenView("home", "HomeScreen")
            HomeScreen(
                authViewModel = authViewModel,
                analyticsHelper = analyticsHelper,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}