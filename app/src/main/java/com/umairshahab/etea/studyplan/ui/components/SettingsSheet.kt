package com.umairshahab.etea.studyplan.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.BuildConfig
import com.umairshahab.etea.studyplan.R
import com.umairshahab.etea.studyplan.ui.theme.PrimaryGradientBrush
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults
import com.umairshahab.etea.studyplan.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onEnableBackgroundAlerts: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = StudyPlanThemeDefaults.glassColors.isDark
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    var showAutostartGuide by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0B1329) else Color(0xFFF8FAFC),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Theme section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Theme",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        label = "Light",
                        iconRes = R.drawable.ic_theme_light,
                        isSelected = themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = "Dark",
                        iconRes = R.drawable.ic_theme_dark,
                        isSelected = themeMode == ThemeMode.DARK,
                        onClick = { onThemeModeChange(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = "System",
                        iconRes = R.drawable.ic_theme_system,
                        isSelected = themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Backup & Data section
            val context = LocalContext.current
            val lastBackupExportedAt = remember(context) {
                context.getSharedPreferences("study_plan_prefs", Context.MODE_PRIVATE)
                    .getLong("last_backup_exported_at", 0L)
            }
            val lastBackupCaption = if (lastBackupExportedAt <= 0L) {
                "Last backup: never"
            } else {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                "Last backup: ${sdf.format(Date(lastBackupExportedAt))}"
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Backup & Storage",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = lastBackupCaption,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )

                SettingsActionCard(
                    title = "Export backup",
                    description = "Save study topics and revision schedule to JSON",
                    onClick = {
                        onDismiss()
                        onExportBackup()
                    }
                )

                SettingsActionCard(
                    title = "Import backup",
                    description = "Restore study topics and schedule from JSON file",
                    onClick = {
                        onDismiss()
                        onImportBackup()
                    }
                )
            }

            // Alerts & Notifications section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Alerts & Notifications",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                SettingsActionCard(
                    title = "Enable background alerts",
                    description = "Exempt Study Plan from battery optimization to ensure alarms fire reliably",
                    onClick = {
                        onDismiss()
                        onEnableBackgroundAlerts()
                    }
                )

                SettingsActionCard(
                    title = "Autostart & battery guide",
                    description = "Step-by-step setup for Infinix, Tecno, Xiaomi, Samsung, and Android devices",
                    onClick = {
                        showAutostartGuide = true
                    }
                )
            }

            // About section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = StudyPlanThemeDefaults.glassColors.cardBorder,
                colors = CardDefaults.cardColors(
                    containerColor = StudyPlanThemeDefaults.glassColors.cardSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "About Study Plan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Offline-first spaced repetition engine designed for ETEA entrance test preparation. All data is stored locally on your device.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    if (showAutostartGuide) {
        AutostartGuideSheet(
            onDismiss = { showAutostartGuide = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutostartGuideSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = StudyPlanThemeDefaults.glassColors.isDark
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0B1329) else Color(0xFFF8FAFC),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Autostart & battery guide",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close guide",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Device manufacturers apply aggressive background restrictions that can delay or silence spaced repetition alarms. Follow the instructions for your device to ensure reminders arrive on schedule.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            VendorGuideCard(
                vendorTitle = "Infinix XOS",
                autostartStep = "Open Settings > Phone Master (or App Management) > Auto-start Management. Locate Study Plan and enable the toggle.",
                batteryStep = "Open Settings > Battery Lab > Battery Optimization > All apps > Study Plan. Select Don't optimize.",
                backgroundStep = "Open Settings > Apps > Study Plan > Battery. Select Allow background activity."
            )

            VendorGuideCard(
                vendorTitle = "Tecno HiOS",
                autostartStep = "Open Settings > App Management > Auto-start. Enable the toggle for Study Plan.",
                batteryStep = "Open Settings > Battery Lab > Battery Optimization > select Study Plan. Select Don't optimize.",
                backgroundStep = "Open Settings > App Management > Study Plan > Battery > Background running. Select Allow."
            )

            VendorGuideCard(
                vendorTitle = "Xiaomi MIUI",
                autostartStep = "Open Settings > Apps > Permissions > Autostart. Turn on the toggle for Study Plan.",
                batteryStep = "Open Settings > Apps > Manage Apps > Study Plan > Battery saver. Select No restrictions.",
                backgroundStep = "In the App Info screen, ensure background activity is permitted. In the Recent Apps overview, tap and hold Study Plan to lock it in memory."
            )

            VendorGuideCard(
                vendorTitle = "Samsung One UI",
                autostartStep = "Open Settings > Apps > Study Plan > Battery. Choose Unrestricted.",
                batteryStep = "Open Settings > Battery and device care > Battery > Background usage limits > Never sleeping apps. Tap + and add Study Plan.",
                backgroundStep = "In Battery settings, verify that Put unused apps to sleep is disabled for Study Plan."
            )

            VendorGuideCard(
                vendorTitle = "Generic Android",
                autostartStep = "If your device settings provide an Autostart, App Launch, or Startup Manager, ensure Study Plan is allowed to run automatically.",
                batteryStep = "Open Settings > Apps > Study Plan > App battery usage (or Battery). Set optimization to Unrestricted or Don't optimize.",
                backgroundStep = "In Study Plan App Info, confirm that Allow background activity and Background data are enabled."
            )
        }
    }
}

@Composable
private fun VendorGuideCard(
    vendorTitle: String,
    autostartStep: String,
    batteryStep: String,
    backgroundStep: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = StudyPlanThemeDefaults.glassColors.cardBorder,
        colors = CardDefaults.cardColors(
            containerColor = StudyPlanThemeDefaults.glassColors.cardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = vendorTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            VendorStepItem(
                stepLabel = "1. Enable autostart",
                instruction = autostartStep
            )

            VendorStepItem(
                stepLabel = "2. Disable battery optimization",
                instruction = batteryStep
            )

            VendorStepItem(
                stepLabel = "3. Allow background activity",
                instruction = backgroundStep
            )
        }
    }
}

@Composable
private fun VendorStepItem(
    stepLabel: String,
    instruction: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stepLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = instruction,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = StudyPlanThemeDefaults.glassColors.isDark
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            StudyPlanThemeDefaults.glassColors.cardSurface
        },
        border = if (isSelected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            StudyPlanThemeDefaults.glassColors.cardBorder
        },
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        border = StudyPlanThemeDefaults.glassColors.cardBorder,
        colors = CardDefaults.cardColors(
            containerColor = StudyPlanThemeDefaults.glassColors.cardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }
    }
}
