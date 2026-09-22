package com.umairshahab.etea.studyplan.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import com.umairshahab.etea.studyplan.R
import com.umairshahab.etea.studyplan.ui.theme.Motion
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults

@Composable
fun BackupReminderCard(
    onExportBackup: () -> Unit,
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
            color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFF0FDF4)
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
                            text = "Protect your progress — export a backup",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Save a JSON file of your topics and study schedule to keep your data safe.",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF14532D),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable(onClick = onExportBackup)
                                .semantics { contentDescription = "Export Backup" },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Export Backup",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                            .semantics { contentDescription = "Dismiss backup reminder" }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Dismiss",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF15803D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
