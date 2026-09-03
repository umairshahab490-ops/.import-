package com.umairshahab.etea.studyplan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import com.umairshahab.etea.studyplan.ui.theme.PrimaryGradientBrush
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HorizontalMonthCalendar(
    revisions: List<RevisionEntity>,
    topics: List<TopicEntity>,
    onMarkDone: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = remember { ZoneId.systemDefault() }
    val now = System.currentTimeMillis()
    val today = remember { LocalDate.now() }
    val startMonth = remember { YearMonth.now() }
    val months = remember(startMonth) {
        (0L..5L).map { startMonth.plusMonths(it) }
    }

    var selectedMonth by remember(startMonth) { mutableStateOf(startMonth) }
    var selectedDateForSheet by remember { mutableStateOf<LocalDate?>(null) }
    val topicMap = remember(topics) { topics.associateBy { it.id } }

    val revisionsByDate = remember(revisions, zone) {
        revisions.filter { it.status == "SCHEDULED" }
            .groupBy { Instant.ofEpochMilli(it.dueAt).atZone(zone).toLocalDate() }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // LEVEL 1: Month Strip (1x4 visible)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // cardWidth = (screenWidth - 32.dp outer padding - 3 x 8.dp spacing) / 4
            val cardWidth = (maxWidth - 32.dp - 24.dp) / 4

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(months, key = { it.toString() }) { month ->
                    CompactMonthCard(
                        month = month,
                        isSelected = month == selectedMonth,
                        revisions = revisions,
                        revisionsByDate = revisionsByDate,
                        now = now,
                        zone = zone,
                        cardWidth = cardWidth,
                        onClick = { selectedMonth = month }
                    )
                }
            }
        }

        // LEVEL 2: Selected Month Detail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SelectedMonthDetailCard(
                month = selectedMonth,
                revisionsByDate = revisionsByDate,
                today = today,
                now = now,
                onDayClick = { date -> selectedDateForSheet = date }
            )
        }
    }

    selectedDateForSheet?.let { date ->
        val currentDayRevisions = revisionsByDate[date] ?: emptyList()
        DayRevisionsSheet(
            date = date,
            revisions = currentDayRevisions,
            topicMap = topicMap,
            onDismiss = { selectedDateForSheet = null },
            onMarkDone = onMarkDone
        )
    }
}

@Composable
fun CompactMonthCard(
    month: YearMonth,
    isSelected: Boolean,
    revisions: List<RevisionEntity>,
    revisionsByDate: Map<LocalDate, List<RevisionEntity>>,
    now: Long,
    zone: ZoneId,
    cardWidth: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthCount = remember(revisions, month, zone) {
        revisions.count { rev ->
            rev.status == "SCHEDULED" &&
                YearMonth.from(Instant.ofEpochMilli(rev.dueAt).atZone(zone)) == month
        }
    }

    val firstDayOfMonth = remember(month) { month.atDay(1) }
    val startOffset = remember(firstDayOfMonth) { firstDayOfMonth.dayOfWeek.value - 1 }
    val daysInMonth = remember(month) { month.lengthOfMonth() }
    val totalSlots = startOffset + daysInMonth

    val cardBorder = if (isSelected) {
        BorderStroke(2.dp, PrimaryGradientBrush)
    } else {
        StudyPlanThemeDefaults.glassColors.cardBorder
    }

    Card(
        modifier = modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = StudyPlanThemeDefaults.glassColors.cardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            // Header: Month & Year on left, Count pill on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = month.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = month.format(DateTimeFormatter.ofPattern("yy", Locale.getDefault())),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (monthCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(brush = PrimaryGradientBrush)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "0",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini 7-column dot matrix (NO day numbers)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (week in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val slotIndex = week * 7 + dayOfWeek
                            if (slotIndex < startOffset || slotIndex >= totalSlots) {
                                Spacer(modifier = Modifier.size(4.dp))
                            } else {
                                val dayNum = slotIndex - startOffset + 1
                                val date = month.atDay(dayNum)
                                val dayRevisions = revisionsByDate[date] ?: emptyList()
                                val hasMissed = dayRevisions.any { it.status == "SCHEDULED" && it.dueAt < now }
                                val hasScheduled = dayRevisions.any { it.status == "SCHEDULED" }

                                if (hasMissed) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                } else if (hasScheduled) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(brush = PrimaryGradientBrush)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedMonthDetailCard(
    month: YearMonth,
    revisionsByDate: Map<LocalDate, List<RevisionEntity>>,
    today: LocalDate,
    now: Long,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = remember(month) {
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }

    val firstDayOfMonth = remember(month) { month.atDay(1) }
    val startOffset = remember(firstDayOfMonth) { firstDayOfMonth.dayOfWeek.value - 1 }
    val daysInMonth = remember(month) { month.lengthOfMonth() }
    val totalSlots = startOffset + daysInMonth
    val numWeeks = (totalSlots + 6) / 7

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = StudyPlanThemeDefaults.glassColors.cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = StudyPlanThemeDefaults.glassColors.cardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header "MMMM yyyy" with gradient text
            Text(
                text = monthTitle,
                style = TextStyle(
                    brush = PrimaryGradientBrush,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Days of week header (M T W T F S S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Week rows with day numbers (12-13sp), row height >= 44dp
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (week in 0 until numWeeks) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val slotIndex = week * 7 + dayOfWeek
                            if (slotIndex < startOffset || slotIndex >= totalSlots) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                )
                            } else {
                                val dayNum = slotIndex - startOffset + 1
                                val date = month.atDay(dayNum)
                                val dayRevisions = revisionsByDate[date] ?: emptyList()

                                DayDetailCell(
                                    date = date,
                                    dayNum = dayNum,
                                    isToday = date == today,
                                    dayRevisions = dayRevisions,
                                    now = now,
                                    onDayClick = { onDayClick(date) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailCell(
    date: LocalDate,
    dayNum: Int,
    isToday: Boolean,
    dayRevisions: List<RevisionEntity>,
    now: Long,
    onDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasRevisions = dayRevisions.isNotEmpty()
    val hasMissed = remember(dayRevisions, now) {
        dayRevisions.any { it.status == "SCHEDULED" && it.dueAt < now }
    }

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (hasRevisions) {
                    Modifier.clickable(onClick = onDayClick)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (isToday) {
                            Modifier.background(
                                brush = PrimaryGradientBrush,
                                shape = CircleShape
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayNum.toString(),
                    fontSize = 13.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (hasMissed) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .semantics { contentDescription = "Missed revision on day $dayNum" }
                )
            } else if (hasRevisions) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(brush = PrimaryGradientBrush)
                        .semantics { contentDescription = "Scheduled revision on day $dayNum" }
                )
            } else {
                Spacer(modifier = Modifier.size(5.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayRevisionsSheet(
    date: LocalDate,
    revisions: List<RevisionEntity>,
    topicMap: Map<Long, TopicEntity>,
    onDismiss: () -> Unit,
    onMarkDone: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = StudyPlanThemeDefaults.glassColors.isDark
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formattedDate = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()))
    }

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
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Revisions for $formattedDate",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (revisions.isNotEmpty()) "${revisions.size} revision${if (revisions.size > 1) "s" else ""}" else "Completed",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (revisions.isEmpty()) {
                Text(
                    text = "All revisions completed for this day! 🎉",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                val now = System.currentTimeMillis()
                revisions.forEach { rev ->
                    val topic = topicMap[rev.topicId]
                    RevisionRowItem(
                        topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                        subject = topic?.subject ?: "",
                        formattedDue = RevisionScheduler.format(rev.dueAt),
                        isMissed = rev.status == "SCHEDULED" && rev.dueAt < now,
                        onDone = { onMarkDone(rev.id) }
                    )
                }
            }
        }
    }
}
