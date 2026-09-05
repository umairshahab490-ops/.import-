package com.umairshahab.etea.studyplan.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.R
import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.ui.theme.Motion
import com.umairshahab.etea.studyplan.ui.theme.PrimaryGradientBrush
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults

fun formatChapterSubtitle(chapter: String?, subject: String): String {
    if (chapter.isNullOrBlank()) return subject
    val trimmed = chapter.trim()
    val chapterText = if (trimmed.startsWith("chapter", ignoreCase = true)) {
        val remainder = trimmed.substring(7).trim()
        if (remainder.isNotEmpty()) "Chapter $remainder" else "Chapter"
    } else {
        "Chapter $trimmed"
    }
    return "$chapterText • $subject"
}

@Composable
fun StaggeredCardEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val density = LocalDensity.current
    val slideOffset = remember(density) { with(density) { 8.dp.roundToPx() } }
    val delayMs = (index.coerceAtMost(6) * 40)

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = Motion.MEDIUM,
                delayMillis = delayMs,
                easing = Motion.DECELERATE
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = Motion.MEDIUM,
                delayMillis = delayMs,
                easing = Motion.DECELERATE
            ),
            initialOffsetY = { slideOffset }
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun MetricCard(
    label: String,
    count: Int,
    containerColor: Color = StudyPlanThemeDefaults.glassColors.cardSurface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onClick)
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        border = StudyPlanThemeDefaults.glassColors.cardBorder,
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically(
                            animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD),
                            initialOffsetY = { -it }
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                        )).togetherWith(
                            slideOutVertically(
                                animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD),
                                targetOffsetY = { it }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                            )
                        )
                    } else {
                        (slideInVertically(
                            animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD),
                            initialOffsetY = { it }
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                        )).togetherWith(
                            slideOutVertically(
                                animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD),
                                targetOffsetY = { -it }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                            )
                        )
                    }
                },
                label = "metric_count_animation"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlaceholderCalendarCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = StudyPlanThemeDefaults.glassColors.cardBorder,
        color = StudyPlanThemeDefaults.glassColors.cardSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_empty_calendar),
                    contentDescription = "Calendar view upcoming",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Calendar View",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Calendar arrives later. Track daily targets right here!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun RevisionBadge(
    intervalIndex: Int,
    intervalDays: Int,
    modifier: Modifier = Modifier
) {
    val isDark = StudyPlanThemeDefaults.glassColors.isDark
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.8f) else Color(0xFFEFF6FF),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color(0xFFBFDBFE)),
        modifier = modifier
    ) {
        Text(
            text = "Rev ${intervalIndex + 1} · +${intervalDays}d",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TopicRowItem(
    topic: TopicEntity,
    nextDueFormatted: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    nextRevision: RevisionEntity? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = StudyPlanThemeDefaults.glassColors.cardBorder,
        color = StudyPlanThemeDefaults.glassColors.cardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val subtitle = formatChapterSubtitle(topic.chapter, topic.subject)
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (nextRevision != null && nextDueFormatted != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Next due: $nextDueFormatted",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            RevisionBadge(
                                intervalIndex = nextRevision.intervalIndex,
                                intervalDays = nextRevision.intervalDays
                            )
                        }
                    } else {
                        val duePart = nextDueFormatted?.let { "Next due: $it" } ?: "All revisions completed"
                        Text(
                            text = duePart,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = CircleShape,
                    modifier = Modifier
                        .semantics { contentDescription = "Edit topic" }
                        .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                ) {
                    Text("Edit", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onDelete,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .semantics { contentDescription = "Delete topic" }
                        .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                ) {
                    Text("Delete", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun RevisionRowItem(
    topicTitle: String,
    subject: String,
    formattedDue: String,
    isMissed: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    chapter: String? = null,
    intervalIndex: Int? = null,
    intervalDays: Int? = null,
    statusText: String? = null,
    buttonText: String = "Done"
) {
    val isDark = StudyPlanThemeDefaults.glassColors.isDark
    val containerColor = if (isMissed) {
        if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.35f) else Color(0xFFFEF2F2).copy(alpha = 0.85f)
    } else {
        StudyPlanThemeDefaults.glassColors.cardSurface
    }
    val borderColor = if (isMissed) {
        if (isDark) Color(0xFFEF4444).copy(alpha = 0.35f) else Color(0xFFFCA5A5).copy(alpha = 0.60f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.30f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topicTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = formatChapterSubtitle(chapter, subject)
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isMissed) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDark) Color(0xFF991B1B) else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = "MISSED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFEE2E2) else Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (intervalIndex != null && intervalDays != null) {
                        RevisionBadge(
                            intervalIndex = intervalIndex,
                            intervalDays = intervalDays
                        )
                    }
                    if (statusText != null && !isMissed) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    val textColor = if (isMissed) {
                        if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = "Due: $formattedDue",
                        fontSize = 13.sp,
                        color = textColor,
                        fontWeight = if (isMissed) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onDone,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMissed) Color(0xFFDC2626) else Color(0xFF16A34A),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .semantics { contentDescription = "Mark revision done" }
                    .defaultMinSize(minWidth = 68.dp, minHeight = 48.dp)
            ) {
                Text(buttonText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .background(
                brush = if (enabled) PrimaryGradientBrush
                else Brush.horizontalGradient(listOf(Color(0xFF64748B), Color(0xFF94A3B8))),
                shape = CircleShape
            )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
fun GradientPillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) Color.Transparent else StudyPlanThemeDefaults.glassColors.cardSurface,
        border = if (selected) null else StudyPlanThemeDefaults.glassColors.cardBorder,
        modifier = modifier
            .then(
                if (selected) Modifier.background(PrimaryGradientBrush, shape = CircleShape)
                else Modifier
            )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyStateView(
    iconRes: Int = R.drawable.ic_empty_book,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val density = LocalDensity.current
    val slideOffset = remember(density) { with(density) { 8.dp.roundToPx() } }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = Motion.MEDIUM,
                easing = Motion.DECELERATE
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = Motion.MEDIUM,
                easing = Motion.DECELERATE
            ),
            initialOffsetY = { slideOffset }
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
