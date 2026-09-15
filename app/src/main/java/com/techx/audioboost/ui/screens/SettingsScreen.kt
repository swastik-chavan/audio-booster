package com.techx.audioboost.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techx.audioboost.model.AppProfile
import com.techx.audioboost.model.AudioPreset
import com.techx.audioboost.ui.theme.BgColor
import com.techx.audioboost.ui.theme.ErrorColor
import com.techx.audioboost.ui.theme.PrimaryNeon
import com.techx.audioboost.ui.theme.SurfaceColor
import com.techx.audioboost.ui.theme.TextPrimary
import com.techx.audioboost.ui.theme.TextSecondary
import com.techx.audioboost.util.AppProfileManager
import com.techx.audioboost.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val appProfileManager = remember(context) { AppProfileManager(context) }

    val appTheme by viewModel.settingsRepository.appTheme.collectAsState(initial = "audio_booster")
    val hapticsEnabled by viewModel.settingsRepository.hapticsEnabled.collectAsState(initial = true)
    val autoOffMinutes by viewModel.settingsRepository.autoOffMinutes.collectAsState(initial = 0)
    val notificationEnabled by viewModel.settingsRepository.notificationEnabled.collectAsState(initial = true)
    val isLimiterEnabled by viewModel.settingsRepository.isLimiterEnabled.collectAsState(initial = true)
    val isSafetyEnabled by viewModel.settingsRepository.safetyLimitEnabled.collectAsState(initial = true)

    val appProfilesEnabled by viewModel.settingsRepository.appProfilesEnabled.collectAsState(initial = false)
    val appProfiles by viewModel.settingsRepository.appProfiles.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset All Settings?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will restore default boost, equalizer curves, built-in presets, and custom settings.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showAddProfileDialog) {
        AddAppProfileDialog(
            installedApps = remember { appProfileManager.getInstalledLaunchableApps() },
            availablePresets = uiState.presetsList,
            onSave = { profile ->
                viewModel.saveAppProfile(profile, appProfiles)
                showAddProfileDialog = false
            },
            onDismiss = { showAddProfileDialog = false }
        )
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Section
            SettingsSectionHeader("Appearance")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setAppTheme("audio_booster") }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = appTheme == "audio_booster",
                        onClick = { viewModel.setAppTheme("audio_booster") },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryNeon)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Audio Booster Signature", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Dark background with high-contrast neon amber accents", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setAppTheme("dynamic_system") }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = appTheme == "dynamic_system",
                            onClick = { viewModel.setAppTheme("dynamic_system") },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryNeon)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Dynamic System (Material You)", color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Subtle system wallpaper accents with dark background", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Haptics & Feedback
            SettingsSectionHeader("Haptics & Feedback")
            SettingsToggleRow(
                title = "Haptic Feedback",
                subtitle = "Subtle tactile response when adjusting sliders at 10% steps and entering Ultra Mode",
                checked = hapticsEnabled,
                onCheckedChange = { viewModel.setHapticsEnabled(it) }
            )

            // Audio Safety & Limiter
            SettingsSectionHeader("Distortion & Safety")
            SettingsToggleRow(
                title = "Distortion Protection Limiter",
                subtitle = "Dynamic headroom management to prevent harsh digital clipping and crackling",
                checked = isLimiterEnabled,
                onCheckedChange = { viewModel.toggleLimiter(it) }
            )
            SettingsToggleRow(
                title = "Hardware-Aware Safe Limits",
                subtitle = "Enforces conservative amplification limits for phone speakers and headphones",
                checked = isSafetyEnabled,
                onCheckedChange = { viewModel.toggleSafetyLimit(it) }
            )

            // Power & Background
            SettingsSectionHeader("Power & System")
            SettingsToggleRow(
                title = "Ongoing Status Notification",
                subtitle = "Keep quick volume controls visible in the notification shade when active",
                checked = notificationEnabled,
                onCheckedChange = { viewModel.setNotificationEnabled(it) }
            )

            // Auto-off
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Auto-Off on Inactivity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Disables audio processing when media playback stops to conserve battery",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "Off", 5 to "5 min", 15 to "15 min", 30 to "30 min").forEach { (mins, label) ->
                        val isSelected = autoOffMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryNeon else Color(0xFF1B1B22))
                                .border(1.dp, if (isSelected) PrimaryNeon else Color(0xFF282834), RoundedCornerShape(8.dp))
                                .clickable { viewModel.setAutoOffMinutes(mins) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0B0B0D) else TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Per-App Audio Profiles Section
            SettingsSectionHeader("Per-App Audio Profiles")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .border(1.dp, Color(0xFF282834), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable App-Aware Profiles",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Automatically apply assigned presets when selected apps become active",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = appProfilesEnabled,
                        onCheckedChange = { viewModel.setAppProfilesEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryNeon,
                            checkedBorderColor = PrimaryNeon
                        )
                    )
                }

                val hasUsageAccess = remember { appProfileManager.hasUsageAccess() }

                if (!hasUsageAccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B1B22), RoundedCornerShape(10.dp))
                            .border(1.dp, PrimaryNeon.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Permission Info",
                                    tint = PrimaryNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Permission Required",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = PrimaryNeon,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = "Android requires Usage Access permission to detect foreground apps. Tap below to enable it in Android Settings.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            )
                            TextButton(
                                onClick = {
                                    try {
                                        context.startActivity(appProfileManager.getUsageAccessSettingsIntent())
                                    } catch (t: Throwable) {
                                    }
                                }
                            ) {
                                Text("Grant Usage Access", color = PrimaryNeon, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "✓ Android Usage Access Granted",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryNeon,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Configured Profiles List
                if (appProfiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configured Profiles",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    for (profile in appProfiles) {
                        val preset = uiState.presetsList.firstOrNull { it.id == profile.presetId }
                        val presetName = preset?.name ?: "Flat"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF181820))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.appName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "→ $presetName Preset",
                                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryNeon)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteAppProfile(profile.packageName, appProfiles) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Profile",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { showAddProfileDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("+ Add App Profile", color = PrimaryNeon, fontWeight = FontWeight.Bold)
                }
            }

            // Danger Zone / Reset
            SettingsSectionHeader("Reset")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .border(1.dp, ErrorColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable { showResetDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reset All Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ErrorColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Restore default settings, curves, and presets",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1F1F24), thickness = 1.dp)

            // Open Source & Author Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Audio Booster v2.0",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "Developed by Swastik Chavan • Open Source",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary.copy(alpha = 0.7f))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "GitHub Profile",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryNeon,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onOpenUrl("https://github.com/swastik-chavan") }
                    )
                    Text(
                        text = "Repository",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryNeon,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onOpenUrl("https://github.com/swastik-chavan/audio-booster") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Dialog to configure a new Per-App Audio Profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppProfileDialog(
    installedApps: List<Pair<String, String>>,
    availablePresets: List<AudioPreset>,
    onSave: (AppProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAppIndex by remember { mutableStateOf(0) }
    var selectedPresetIndex by remember { mutableStateOf(0) }

    var appExpanded by remember { mutableStateOf(false) }
    var presetExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add App Profile",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select an installed application and the sound preset to automatically activate when it opens.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                // App Selector
                if (installedApps.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = appExpanded,
                        onExpandedChange = { appExpanded = !appExpanded }
                    ) {
                        OutlinedTextField(
                            value = installedApps.getOrNull(selectedAppIndex)?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Application", color = TextSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = appExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryNeon,
                                unfocusedBorderColor = Color(0xFF282834),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = appExpanded,
                            onDismissRequest = { appExpanded = false },
                            modifier = Modifier.background(SurfaceColor)
                        ) {
                            installedApps.forEachIndexed { index, app ->
                                DropdownMenuItem(
                                    text = { Text(app.second, color = TextPrimary) },
                                    onClick = {
                                        selectedAppIndex = index
                                        appExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text("No launchable applications detected.", color = TextSecondary, fontSize = 12.sp)
                }

                // Preset Selector
                ExposedDropdownMenuBox(
                    expanded = presetExpanded,
                    onExpandedChange = { presetExpanded = !presetExpanded }
                ) {
                    OutlinedTextField(
                        value = availablePresets.getOrNull(selectedPresetIndex)?.name ?: "Flat",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Preset", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNeon,
                            unfocusedBorderColor = Color(0xFF282834),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = presetExpanded,
                        onDismissRequest = { presetExpanded = false },
                        modifier = Modifier.background(SurfaceColor)
                    ) {
                        availablePresets.forEachIndexed { index, preset ->
                            DropdownMenuItem(
                                text = { Text(preset.name, color = TextPrimary) },
                                onClick = {
                                    selectedPresetIndex = index
                                    presetExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val app = installedApps.getOrNull(selectedAppIndex)
                    val preset = availablePresets.getOrNull(selectedPresetIndex)
                    if (app != null && preset != null) {
                        onSave(
                            AppProfile(
                                packageName = app.first,
                                appName = app.second,
                                presetId = preset.id
                            )
                        )
                    }
                },
                enabled = installedApps.isNotEmpty()
            ) {
                Text("Save Profile", color = PrimaryNeon, fontWeight = FontWeight.Bold)
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
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            color = PrimaryNeon,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryNeon,
                checkedBorderColor = PrimaryNeon,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Color(0xFF1F1F24),
                uncheckedBorderColor = Color(0xFF2E2E36)
            )
        )
    }
}
