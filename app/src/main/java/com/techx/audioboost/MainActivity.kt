package com.techx.audioboost

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.techx.audioboost.service.AudioService
import com.techx.audioboost.ui.screens.MainScreen
import com.techx.audioboost.ui.theme.AudioBoostTheme
import com.techx.audioboost.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        handleNotificationIntent(intent)
        checkNotificationPermission()

        enableEdgeToEdge()
        setContent {
            val appTheme by viewModel.settingsRepository.appTheme.collectAsState(initial = "audio_booster")
            AudioBoostTheme(appTheme = appTheme) {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        when (intent?.action) {
            AudioService.ACTION_TOGGLE_BOOST -> {
                val currentBoost = viewModel.uiState.value.boostPercent
                viewModel.updateBoostPercent(if (currentBoost > 0) 0 else 50)
            }
            AudioService.ACTION_TOGGLE_ULTRA -> {
                val isUltra = viewModel.uiState.value.isUltraModeEnabled
                viewModel.toggleUltraMode(!isUltra)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::viewModel.isInitialized) {
            viewModel.onActivityDestroyed(isChangingConfigurations)
        }
    }
}