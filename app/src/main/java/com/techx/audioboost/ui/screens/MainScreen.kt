package com.techx.audioboost.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techx.audioboost.R
import com.techx.audioboost.audio.DeviceType
import com.techx.audioboost.model.AudioPreset
import com.techx.audioboost.ui.theme.BgColor
import com.techx.audioboost.ui.theme.DisabledColor
import com.techx.audioboost.ui.theme.ErrorColor
import com.techx.audioboost.ui.theme.PrimaryNeon
import com.techx.audioboost.ui.theme.SurfaceColor
import com.techx.audioboost.ui.theme.SurfaceElevated
import com.techx.audioboost.ui.theme.TextPrimary
import com.techx.audioboost.ui.theme.TextSecondary
import com.techx.audioboost.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var currentScreen by rememberSaveable { mutableStateOf("main") }
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showGithubDialog by rememberSaveable { mutableStateOf(false) }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }
    var showSplash by rememberSaveable { mutableStateOf(true) }

    var isEqExpanded by rememberSaveable { mutableStateOf(false) }
    var isStatusExpanded by rememberSaveable { mutableStateOf(false) }

    val safeOpenUrl = remember(context) {
        { url: String ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No web browser found to open link.", Toast.LENGTH_LONG).show()
            } catch (t: Throwable) {
                Toast.makeText(context, "Unable to open link.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (currentScreen == "settings") {
        SettingsScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "main" },
            onOpenUrl = safeOpenUrl
        )
        return
    }

    if (showWarningDialog) {
        UltraWarningDialog(
            onConfirm = {
                viewModel.hasShownUltraWarning = true
                viewModel.toggleUltraMode(true)
                showWarningDialog = false
            },
            onDismiss = {
                showWarningDialog = false
            }
        )
    }

    if (showInfoDialog) {
        InfoDialog(
            onDismiss = { showInfoDialog = false },
            onOpenUrl = safeOpenUrl
        )
    }

    if (showGithubDialog) {
        GithubDialog(
            onDismiss = { showGithubDialog = false },
            onOpenUrl = safeOpenUrl
        )
    }

    if (showSavePresetDialog) {
        SavePresetDialog(
            onSave = { name ->
                viewModel.saveCustomPreset(name)
                showSavePresetDialog = false
            },
            onDismiss = { showSavePresetDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = BgColor
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Audio Booster",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 28.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            HeaderEqualizerIcon(
                                tint = PrimaryNeon,
                                modifier = Modifier.size(width = 22.dp, height = 18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Precision loudness & multi-effect suite",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(onClick = { currentScreen = "settings" }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = PrimaryNeon,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About Info",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { showGithubDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "Open Source GitHub",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Hero Boost & Audio Visualizer Section
                HeroBoostVisualizerCard(
                    boostPercent = state.boostPercent,
                    gainmB = state.loudnessGainmB,
                    isUltra = state.isUltraModeEnabled,
                    isMediaPlaying = state.isMediaPlaying
                )

                // Media Volume Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Media Volume",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${state.systemVolumePercent}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryNeon
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    PremiumSlider(
                        value = state.systemVolumePercent.toFloat(),
                        onValueChange = { viewModel.updateSystemVolume(it.toInt()) },
                        activeColor = PrimaryNeon
                    )
                }

                // Boost Volume Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Boost Volume",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (state.isBoostSupported) TextPrimary else DisabledColor
                            )
                        )
                        if (state.isBoostSupported) {
                            Text(
                                text = "${state.boostPercent}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryNeon
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (state.isBoostSupported) {
                        PremiumSlider(
                            value = state.boostPercent.toFloat(),
                            onValueChange = { viewModel.updateBoostPercent(it.toInt()) },
                            activeColor = PrimaryNeon
                        )
                    } else {
                        Text(
                            text = "This device does not support hardware loudness enhancement.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ErrorColor)
                        )
                    }
                }

                // Ultra Volume Section
                if (state.isBoostSupported) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceColor)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ultra Volume Mode",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = if (state.isUltraModeLocked) DisabledColor else TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "High-gain amplification beyond normal safe ceiling",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (state.isUltraModeLocked) DisabledColor else TextSecondary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = state.isUltraModeEnabled && !state.isUltraModeLocked,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        if (!viewModel.hasShownUltraWarning) {
                                            showWarningDialog = true
                                        } else {
                                            viewModel.toggleUltraMode(true)
                                        }
                                    } else {
                                        viewModel.toggleUltraMode(false)
                                    }
                                },
                                enabled = !state.isUltraModeLocked,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ErrorColor,
                                    checkedBorderColor = ErrorColor,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Color(0xFF1F1F24),
                                    uncheckedBorderColor = Color(0xFF2E2E36)
                                )
                            )
                        }

                        if (state.isUltraModeLocked) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Increase Boost Volume to unlock Ultra Volume.",
                                style = MaterialTheme.typography.labelSmall.copy(color = PrimaryNeon)
                            )
                        }

                        AnimatedVisibility(
                            visible = state.isUltraModeEnabled && !state.isUltraModeLocked,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ultra Boost Level",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = ErrorColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${state.ultraBoostPercent}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = ErrorColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                PremiumSlider(
                                    value = state.ultraBoostPercent.toFloat(),
                                    onValueChange = { viewModel.updateUltraBoostPercent(it.toInt()) },
                                    activeColor = ErrorColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⚠ High amplification can stress speakers and strain hearing.",
                                    style = MaterialTheme.typography.labelSmall.copy(color = ErrorColor)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1F1F24), thickness = 1.dp)

                // Sound Presets Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sound Presets",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "+ Save Custom",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryNeon,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable { showSavePresetDialog = true }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (preset in state.presetsList) {
                            val isSelected = preset.id == state.activePresetId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PrimaryNeon else SurfaceColor)
                                    .border(
                                        1.dp,
                                        if (isSelected) PrimaryNeon else Color(0xFF282834),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.applyPreset(preset) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isSelected) Color(0xFF0B0B0D) else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                    if (preset.isCustom) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete",
                                            tint = if (isSelected) Color(0xFF0B0B0D) else TextSecondary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.deleteCustomPreset(preset.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Multi-Band Equalizer (Collapsible)
                if (state.isEqualizerSupported) {
                    EqualizerSectionCard(
                        isExpanded = isEqExpanded,
                        onToggleExpand = { isEqExpanded = !isEqExpanded },
                        isEnabled = state.isEqEnabled,
                        onToggleEnabled = { viewModel.toggleEqualizer(it) },
                        bands = state.eqBands,
                        bandLevels = state.eqBandLevels,
                        minLevel = state.eqMinLevelmB,
                        maxLevel = state.eqMaxLevelmB,
                        onBandLevelChange = { index, level -> viewModel.updateEqualizerBand(index, level) },
                        onResetFlat = { viewModel.resetEqualizerToFlat() }
                    )
                }

                // Bass Boost & Virtualizer Compact Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bass Boost
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceColor)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bass Boost",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.isBassBoostSupported) TextPrimary else DisabledColor
                                )
                            )
                            Switch(
                                checked = state.isBassBoostEnabled,
                                onCheckedChange = { viewModel.updateBassBoost(it, state.bassBoostStrength) },
                                enabled = state.isBassBoostSupported,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryNeon,
                                    checkedBorderColor = PrimaryNeon
                                )
                            )
                        }
                        if (state.isBassBoostSupported && state.isBassBoostEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            PremiumSlider(
                                value = (state.bassBoostStrength / 10).toFloat(),
                                onValueChange = { viewModel.updateBassBoost(true, (it * 10).toInt()) },
                                activeColor = PrimaryNeon
                            )
                            Text(
                                text = "${state.bassBoostStrength / 10}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = PrimaryNeon)
                            )
                        }
                    }

                    // Virtualizer
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceColor)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Virtualizer",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.isVirtualizerSupported) TextPrimary else DisabledColor
                                )
                            )
                            Switch(
                                checked = state.isVirtualizerEnabled,
                                onCheckedChange = { viewModel.updateVirtualizer(it, state.virtualizerStrength) },
                                enabled = state.isVirtualizerSupported,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryNeon,
                                    checkedBorderColor = PrimaryNeon
                                )
                            )
                        }
                        if (state.isVirtualizerSupported && state.isVirtualizerEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            PremiumSlider(
                                value = (state.virtualizerStrength / 10).toFloat(),
                                onValueChange = { viewModel.updateVirtualizer(true, (it * 10).toInt()) },
                                activeColor = PrimaryNeon
                            )
                            Text(
                                text = "${state.virtualizerStrength / 10}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = PrimaryNeon)
                            )
                        }
                    }
                }

                // Safety Limiter Status Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceColor)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distortion Limiter",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        val limiterStatus = if (state.isLimiterEnabled) {
                            if (state.limiterReductionmB > 0) {
                                "Limiting: -${String.format("%.1f", state.limiterReductionmB / 100f)} dB"
                            } else {
                                "Active (Headroom Safe)"
                            }
                        } else {
                            "Disabled"
                        }
                        Text(
                            text = limiterStatus,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (state.isLimiterEnabled) PrimaryNeon else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    val safeCeilingDb = state.safeLimitCeilingmB / 100f
                    Text(
                        text = "Hardware Safe Ceiling: +${String.format("%.1f", safeCeilingDb)} dB (${state.currentDeviceName})",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                // Collapsible System Status & Diagnostics
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceColor)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isStatusExpanded = !isStatusExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "System Status & Hardware",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Icon(
                            imageVector = if (isStatusExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Status",
                            tint = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val outputTypeLabel = when (state.currentDeviceType) {
                        DeviceType.SPEAKER -> "Speaker"
                        DeviceType.WIRED_HEADSET -> "Wired"
                        DeviceType.BLUETOOTH -> "Bluetooth"
                        DeviceType.USB -> "USB"
                        DeviceType.UNKNOWN -> "Other"
                    }

                    InfoRow(label = "Output Device", value = "${state.currentDeviceName} ($outputTypeLabel)")
                    InfoRow(
                        label = "Processing State",
                        value = if (state.boostPercent > 0) "Active" else "Standby",
                        valueColor = if (state.boostPercent > 0) PrimaryNeon else TextSecondary
                    )

                    AnimatedVisibility(visible = isStatusExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HorizontalDivider(color = Color(0xFF1F1F24), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                            InfoRow(label = "Boost Level", value = "${state.boostPercent}%" + if (state.isUltraModeEnabled) " (Ultra ${state.ultraBoostPercent}%)" else "")
                            InfoRow(label = "Active Preset", value = state.activePresetName)
                            InfoRow(label = "Boost Mode", value = if (state.isUltraModeEnabled) "Ultra Volume" else "Normal Boost")
                            InfoRow(label = "Effective Gain", value = "+${String.format("%.1f", state.loudnessGainmB / 100f)} dB")
                            InfoRow(label = "Limiter Attenuation", value = if (state.limiterReductionmB > 0) "-${String.format("%.1f", state.limiterReductionmB / 100f)} dB" else "0.0 dB (Headroom Safe)")
                            InfoRow(label = "Hardware Safe Ceiling", value = "+${String.format("%.1f", state.safeLimitCeilingmB / 100f)} dB")
                            InfoRow(label = "Active Sessions", value = "${state.activeSessionsCount}")
                            InfoRow(
                                label = "LoudnessEnhancer",
                                value = if (state.isBoostSupported) "Available" else "Unsupported",
                                valueColor = if (state.isBoostSupported) PrimaryNeon else ErrorColor
                            )
                            InfoRow(
                                label = "Multi-Band EQ",
                                value = if (state.isEqualizerSupported) "${state.eqBands.size} Bands" else "Unsupported",
                                valueColor = if (state.isEqualizerSupported) PrimaryNeon else TextSecondary
                            )
                            InfoRow(
                                label = "BassBoost FX",
                                value = if (state.isBassBoostSupported) "Supported" else "Unsupported",
                                valueColor = if (state.isBassBoostSupported) PrimaryNeon else TextSecondary
                            )
                            InfoRow(
                                label = "Virtualizer FX",
                                value = if (state.isVirtualizerSupported) "Supported" else "Unsupported",
                                valueColor = if (state.isVirtualizerSupported) PrimaryNeon else TextSecondary
                            )
                            InfoRow(
                                label = "Media Stream",
                                value = if (state.isMediaPlaying) "Playing" else "Paused / Idle",
                                valueColor = if (state.isMediaPlaying) PrimaryNeon else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showSplash) {
            SplashScreen(onDismiss = { showSplash = false })
        }
    }
}

/**
 * Hero Boost Display with Dynamic Responsive Visualizer Bars.
 */
@Composable
fun HeroBoostVisualizerCard(
    boostPercent: Int,
    gainmB: Int,
    isUltra: Boolean,
    isMediaPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WavePhase"
    )

    val gainDb = gainmB / 100f
    val activeColor = if (isUltra) ErrorColor else PrimaryNeon

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceColor)
            .border(1.dp, activeColor.copy(alpha = if (boostPercent > 0) 0.35f else 0.15f), RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isUltra) "ULTRA AMPLIFICATION" else if (boostPercent > 0) "LOUDNESS BOOST ACTIVE" else "STANDBY",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (boostPercent > 0) activeColor else TextSecondary,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$boostPercent",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (boostPercent > 0) activeColor else TextPrimary,
                    lineHeight = 54.sp
                )
            )
            Text(
                text = "%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = activeColor,
                    lineHeight = 36.sp
                ),
                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            )
        }

        Text(
            text = if (boostPercent > 0) "+${String.format("%.1f", gainDb)} dB Gain" else "+0.0 dB",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtle dynamic audio waveform visualizer (9 bars)
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(28.dp)
        ) {
            val totalBars = 9
            val gap = 4.dp.toPx()
            val barW = (size.width - (totalBars - 1) * gap) / totalBars
            val h = size.height
            val cy = h / 2f

            for (i in 0 until totalBars) {
                val normalizedIdx = kotlin.math.abs(i - 4) / 4f
                val baseFraction = (1f - normalizedIdx * 0.6f)
                val boostFraction = (boostPercent / 100f).coerceIn(0.15f, 1f)

                val motion = if (isMediaPlaying && boostPercent > 0) {
                    val phaseOffset = (i * 0.35f + wavePhase * 2f)
                    (kotlin.math.sin(phaseOffset.toDouble()).toFloat() * 0.3f + 0.7f)
                } else if (boostPercent > 0) {
                    0.6f
                } else {
                    0.25f
                }

                val barH = (h * baseFraction * boostFraction * motion).coerceIn(4.dp.toPx(), h)
                val x = i * (barW + gap) + barW / 2f

                drawLine(
                    color = if (boostPercent > 0) activeColor.copy(alpha = 0.85f) else DisabledColor,
                    start = Offset(x, cy - barH / 2f),
                    end = Offset(x, cy + barH / 2f),
                    strokeWidth = barW,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Equalizer section card with dynamic band sliders.
 */
@Composable
fun EqualizerSectionCard(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    bands: List<com.techx.audioboost.audio.EqualizerBand>,
    bandLevels: List<Int>,
    minLevel: Int,
    maxLevel: Int,
    onBandLevelChange: (index: Int, levelmB: Int) -> Unit,
    onResetFlat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceColor)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleExpand() }
            ) {
                Text(
                    text = "Equalizer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand Equalizer",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isExpanded) {
                    Text(
                        text = "Flat",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryNeon,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { onResetFlat() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryNeon,
                        checkedBorderColor = PrimaryNeon
                    )
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for ((index, band) in bands.withIndex()) {
                    val currentLevel = if (index < bandLevels.size) bandLevels[index] else 0
                    val currentDb = currentLevel / 100f
                    val dbFormatted = if (currentDb > 0) "+${String.format("%.1f", currentDb)} dB" else "${String.format("%.1f", currentDb)} dB"

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = band.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isEnabled) TextPrimary else DisabledColor
                                )
                            )
                            Text(
                                text = dbFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isEnabled) PrimaryNeon else DisabledColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        PremiumSlider(
                            value = currentLevel.toFloat(),
                            valueRange = minLevel.toFloat()..maxLevel.toFloat(),
                            onValueChange = { onBandLevelChange(index, it.toInt()) },
                            enabled = isEnabled,
                            activeColor = PrimaryNeon
                        )
                    }
                }
            }
        }
    }
}

/**
 * Save Preset Dialog
 */
@Composable
fun SavePresetDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save Custom Preset",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Saves current boost, EQ curve, Bass Boost, and Virtualizer settings.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    placeholder = { Text("Preset Name (e.g. My Bass Setup)", color = DisabledColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = Color(0xFF282834),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (presetName.isNotBlank()) {
                        onSave(presetName)
                    }
                },
                enabled = presetName.isNotBlank()
            ) {
                Text("Save", color = PrimaryNeon, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private val EqualizerHeights = floatArrayOf(0.1f, 0.3f, 0.45f, 0.65f, 0.9f, 0.65f, 0.45f, 0.3f, 0.1f)
private val InactiveTrackColor = Color(0xFF18181F)

@Composable
fun HeaderEqualizerIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gap = 2.dp.toPx()
        val totalBars = 9
        val barWidth = (w - (totalBars - 1) * gap) / totalBars
        val cy = h / 2f

        for (i in 0 until totalBars) {
            val x = i * (barWidth + gap) + barWidth / 2f
            val barH = h * EqualizerHeights[i]
            val y1 = cy - barH / 2f
            val y2 = cy + barH / 2f
            drawLine(
                color = tint,
                start = Offset(x, y1),
                end = Offset(x, y2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    activeColor: Color = PrimaryNeon,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()

    var localValue by remember { mutableStateOf(value) }
    var lastUpdateTime by remember { mutableStateOf(0L) }

    LaunchedEffect(value) {
        if (!isDragged) {
            localValue = value
        }
    }

    val thumbWidth by animateDpAsState(
        targetValue = if (isDragged) 10.dp else 8.dp,
        animationSpec = tween(150),
        label = "ThumbWidth"
    )
    val thumbHeight by animateDpAsState(
        targetValue = if (isDragged) 28.dp else 24.dp,
        animationSpec = tween(150),
        label = "ThumbHeight"
    )

    Slider(
        value = localValue,
        onValueChange = { newValue ->
            localValue = newValue
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 25L) {
                onValueChange(newValue)
                lastUpdateTime = currentTime
            }
        },
        onValueChangeFinished = {
            onValueChange(localValue)
        },
        valueRange = valueRange,
        enabled = enabled,
        modifier = modifier,
        interactionSource = interactionSource,
        track = { sliderPositions ->
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val trackH = size.height
                val w = size.width

                drawRoundRect(
                    color = InactiveTrackColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(w, trackH),
                    cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
                )

                val fraction = (localValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                val activeWidth = w * fraction.coerceIn(0f, 1f)

                drawRoundRect(
                    color = if (enabled) activeColor else DisabledColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(activeWidth, trackH),
                    cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
                )
            }
        },
        thumb = {
            if (enabled) {
                Box(
                    modifier = Modifier
                        .size(width = thumbWidth, height = thumbHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                )
            }
        }
    )
}

@Composable
fun UltraWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(w / 2f, h * 0.1f)
                        lineTo(w * 0.1f, h * 0.85f)
                        lineTo(w * 0.9f, h * 0.85f)
                        close()
                    }
                    drawPath(path, color = ErrorColor)

                    drawLine(
                        color = BgColor,
                        start = Offset(w / 2f, h * 0.4f),
                        end = Offset(w / 2f, h * 0.65f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = BgColor,
                        radius = 1.dp.toPx(),
                        center = Offset(w / 2f, h * 0.75f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Warning",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ultra Volume Mode applies amplification beyond the normal boost level.\n\n" +
                            "Extremely high audio levels may:\n\n" +
                            "• Damage your phone's speakers.\n" +
                            "• Damage headphones or external speakers.\n" +
                            "• Cause permanent hearing damage if used at high volume for extended periods.\n" +
                            "• Produce distortion or clipping depending on your device.\n\n" +
                            "Use this feature only if you understand the risks.\n\n" +
                            "By continuing, you acknowledge that you are using this feature at your own risk. The developer is not responsible for damage resulting from improper use.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "I Understand",
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = TextSecondary
                )
            }
        },
        containerColor = Color(0xFF131317),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun InfoDialog(
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val appLabel = context.applicationInfo.loadLabel(context.packageManager).toString()
    val packageName = context.packageName
    val versionName = try {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: "2.0.0"
    } catch (e: Exception) {
        "2.0.0"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "About Audio Booster",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "A lightweight Android audio booster built using Android's native AudioEffect framework (LoudnessEnhancer, Equalizer, BassBoost, Virtualizer) with hardware safety protection.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoDialogDetailRow(label = "App Name", value = appLabel)
                    InfoDialogDetailRow(label = "Package Name", value = packageName)
                    InfoDialogDetailRow(label = "Version", value = versionName)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryNeon.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, PrimaryNeon.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Caution Warning",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Caution",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNeon
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Use this application responsibly. Excessive amplification may damage speakers, headphones, or hearing. Use at your own risk.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryNeon.copy(alpha = 0.9f),
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onOpenUrl("https://www.youtube.com/@tech.x.0") }) {
                Text(
                    text = "Visit YouTube",
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = TextSecondary
                )
            }
        },
        containerColor = Color(0xFF131317),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun GithubDialog(
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val appLabel = context.applicationInfo.loadLabel(context.packageManager).toString()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Open Source",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoDialogDetailRow(label = "Developer", value = "Swastik Chavan")
                    InfoDialogDetailRow(label = "Application", value = appLabel)
                }

                Text(
                    text = "An open-source Android audio booster built with Jetpack Compose, Kotlin Coroutines, StateFlow, and Android's native AudioEffect APIs.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GitHubLinkRow(
                        label = "GitHub Profile",
                        url = "https://github.com/swastik-chavan",
                        onClick = { onOpenUrl("https://github.com/swastik-chavan") }
                    )
                    GitHubLinkRow(
                        label = "Repository",
                        url = "https://github.com/swastik-chavan/audio-booster",
                        onClick = { onOpenUrl("https://github.com/swastik-chavan/audio-booster") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFF131317),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun InfoDialogDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun GitHubLinkRow(
    label: String,
    url: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF18181F))
            .border(1.dp, Color(0xFF23232C), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_github),
                contentDescription = "GitHub Icon",
                tint = PrimaryNeon,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "External Link Icon",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SplashScreen(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1100)
        visible = false
        delay(250)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(250))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HeaderEqualizerIcon(
                    tint = PrimaryNeon,
                    modifier = Modifier.size(width = 64.dp, height = 48.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "A U D I O",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 8.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "B O O S T E R",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 6.sp
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 2.dp)
                        .background(PrimaryNeon)
                )
            }
        }
    }
}
