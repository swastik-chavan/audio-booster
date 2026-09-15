package com.techx.audioboost.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PremiumDarkColorScheme = darkColorScheme(
    background = BgColor,
    surface = SurfaceColor,
    surfaceVariant = SurfaceElevated,
    primary = PrimaryNeon,
    secondary = SecondaryNeon,
    tertiary = AccentNeon,
    error = ErrorColor,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onPrimary = BgColor,
    onSecondary = BgColor
)

@Composable
fun AudioBoostTheme(
    appTheme: String = "audio_booster",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = if (appTheme == "dynamic_system" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context).copy(
            background = BgColor,
            surface = SurfaceColor
        )
    } else {
        PremiumDarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}