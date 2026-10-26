package com.timdiedrich.rbblfantasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.timdiedrich.rbblfantasy.ui.navigation.Destination
import com.timdiedrich.rbblfantasy.ui.screens.MainScreen
import com.timdiedrich.rbblfantasy.ui.screens.MainViewModel
import com.timdiedrich.rbblfantasy.ui.screens.SplashUiState
import com.timdiedrich.rbblfantasy.ui.theme.RBBLFantasyTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel : MainViewModel by viewModels { MainViewModel.Factory }
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.uiState.value is SplashUiState.Loading
        }


        setContent {
            RBBLFantasyTheme {
                val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
                
                when (val state = uiState) {
                    is SplashUiState.Success -> {
                        MainScreen(
                            startDestination = state.startDestination,
                            mainViewModel = mainViewModel
                        )
                    }
                    is SplashUiState.LoggedOut -> {
                        MainScreen(
                            startDestination = Destination.Login,
                            mainViewModel = mainViewModel
                        )
                    }
                    is SplashUiState.Loading -> {
                        // The splash screen handles the loading state
                    }
                }
            }
        }

    }
}
