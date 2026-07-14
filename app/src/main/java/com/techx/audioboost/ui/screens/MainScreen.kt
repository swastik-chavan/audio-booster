package com.techx.audioboost.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.techx.audioboost.ui.theme.BgColor
import com.techx.audioboost.ui.theme.DisabledColor
import com.techx.audioboost.ui.theme.ErrorColor
import com.techx.audioboost.ui.theme.PrimaryNeon
import com.techx.audioboost.ui.theme.TextPrimary
import com.techx.audioboost.ui.theme.TextSecondary
import com.techx.audioboost.viewmodel.MainViewModel
import kotlin.OptIn
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

    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showGithubDialog by rememberSaveable { mutableStateOf(false) }
    var showSplash by rememberSaveable { mutableStateOf(true) }

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
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Audio Booster",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            HeaderEqualizerIcon(
                                tint = PrimaryNeon,
                                modifier = Modifier.size(width = 24.dp, height = 20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Boost your media audio with precision.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About Info",
                                tint = PrimaryNeon
                            )
                        }
                        IconButton(onClick = { showGithubDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "Open Source GitHub",
                                tint = PrimaryNeon,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

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
                    Spacer(modifier = Modifier.height(12.dp))
                    PremiumSlider(
                        value = state.systemVolumePercent.toFloat(),
                        onValueChange = { viewModel.updateSystemVolume(it.toInt()) },
                        activeColor = PrimaryNeon
                    )
                }

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
                    Spacer(modifier = Modifier.height(12.dp))
                    if (state.isBoostSupported) {
                        PremiumSlider(
                            value = state.boostPercent.toFloat(),
                            onValueChange = { viewModel.updateBoostPercent(it.toInt()) },
                            activeColor = PrimaryNeon
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val gainDb = state.loudnessGainmB / 100f
                        Text(
                            text = "Loudness Gain: +${String.format("%.1f", gainDb)} dB",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondary
                            )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This device does not support Loudness Enhancement.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ErrorColor.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFF1F1F24),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (state.isBoostSupported) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                    text = "Apply additional amplification beyond normal boost.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (state.isUltraModeLocked) DisabledColor else TextSecondary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
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
                                    checkedTrackColor = PrimaryNeon,
                                    checkedBorderColor = PrimaryNeon,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Color(0xFF1F1F24),
                                    uncheckedBorderColor = Color(0xFF2E2E36)
                                )
                            )
                        }
                        if (state.isUltraModeLocked) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Increase Boost Volume to unlock Ultra Volume.",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryNeon,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                if (state.isBoostSupported) {
                    AnimatedVisibility(
                        visible = state.isUltraModeEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Ultra Volume",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorColor
                                        )
                                    )
                                    Text(
                                        text = "Advanced amplification",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary
                                        )
                                    )
                                }
                                Text(
                                    text = "${state.ultraBoostPercent}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = ErrorColor
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            PremiumSlider(
                                value = state.ultraBoostPercent.toFloat(),
                                onValueChange = { viewModel.updateUltraBoostPercent(it.toInt()) },
                                activeColor = ErrorColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠ High amplification may cause audio distortion and can damage speakers or hearing.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = ErrorColor
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = Color(0xFF1F1F24),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "System Status",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val outputTypeLabel = when (state.currentDeviceType) {
                        DeviceType.SPEAKER -> "Speaker"
                        DeviceType.WIRED_HEADSET -> "Wired"
                        DeviceType.BLUETOOTH -> "Bluetooth"
                        DeviceType.USB -> "USB"
                        DeviceType.UNKNOWN -> "Other"
                    }

                    val statusLabel = if (!state.isBoostSupported) {
                        "Unsupported"
                    } else if (state.boostPercent > 0) {
                        "✓ Active"
                    } else {
                        "Inactive"
                    }

                    val statusColor = if (!state.isBoostSupported) {
                        ErrorColor
                    } else if (state.boostPercent > 0) {
                        PrimaryNeon
                    } else {
                        TextSecondary
                    }

                    InfoRow(label = "Device", value = state.currentDeviceName)
                    InfoRow(label = "Output", value = outputTypeLabel)
                    InfoRow(label = "Status", value = statusLabel, valueColor = statusColor)
                }
            }
        }

        if (showSplash) {
            SplashScreen(onDismiss = { showSplash = false })
        }
    }
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
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
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
        val cx = w / 2f
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
                val activeWidth = w * fraction

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
                            "By continuing, you acknowledge that you are using this feature at your own risk. The developer is not responsible for damage to speakers, headphones, hearing, or other hardware resulting from improper use.",
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
        packageInfo.versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
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
                    text = "A lightweight Android audio booster built using Android's LoudnessEnhancer API with adjustable boost controls.",
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
                    InfoDialogDetailRow(label = "Developer", value = "Swastik")
                    InfoDialogDetailRow(label = "Application", value = appLabel)
                }

                Text(
                    text = "An open-source Android audio booster built with Jetpack Compose and Android's LoudnessEnhancer API.",
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
            imageVector = Icons.Default.ArrowForward,
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
        delay(1200)
        visible = false
        delay(300)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(300))
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
