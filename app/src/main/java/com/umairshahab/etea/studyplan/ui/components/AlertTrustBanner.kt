package com.umairshahab.etea.studyplan.ui.components

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.umairshahab.etea.studyplan.R
import com.umairshahab.etea.studyplan.ui.theme.Motion
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults

object AlertTrustHelper {
    const val PREFS_NAME = "study_plan_prefs"
    const val KEY_ALERTS_BANNER_DISMISSED = "alerts_banner_dismissed"

    fun shouldShowBanner(
        context: Context,
        hasScheduledRevisions: Boolean,
        isDismissed: Boolean,
        topicCount: Int = 1
    ): Boolean {
        // 0 topics / no scheduled revisions -> never show
        if (topicCount <= 0 || !hasScheduledRevisions || isDismissed) {
            return false
        }

        // Notification check (API 24+; treat condition as ok when API unavailable)
        val notificationsOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }

        // Exact alarms check (API 31+; treat condition as ok when API unavailable)
        val exactAlarmsOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }

        // Battery optimization exemption check (API 23+; only if one-time prompt was already shown)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val batteryPrompted = prefs.getBoolean("battery_opt_prompted", false)
        val batteryExemptionOk = if (batteryPrompted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true
        }

        return !notificationsOk || !exactAlarmsOk || !batteryExemptionOk
    }
}

@Composable
fun AlertTrustBanner(
    onFixInSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = StudyPlanThemeDefaults.glassColors.isDark

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(Motion.MEDIUM, easing = Motion.DECELERATE)) +
                expandVertically(animationSpec = tween(Motion.MEDIUM, easing = Motion.DECELERATE)),
        exit = fadeOut(animationSpec = tween(Motion.FAST, easing = Motion.STANDARD)) +
                shrinkVertically(animationSpec = tween(Motion.FAST, easing = Motion.STANDARD)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = StudyPlanThemeDefaults.glassColors.cardBorder,
            color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFFFFBEB)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background alerts may be delayed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Notifications, exact alarms, or battery exemption is off. Revisions still appear in the app.",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fix in Settings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = onFixInSettings)
                                .padding(vertical = 2.dp)
                                .semantics { contentDescription = "Fix in Settings" }
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(start = 4.dp)
                            .semantics { contentDescription = "Dismiss alerts banner" }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Dismiss",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
