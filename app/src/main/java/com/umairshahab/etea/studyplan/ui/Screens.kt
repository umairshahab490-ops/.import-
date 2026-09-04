package com.umairshahab.etea.studyplan.ui

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.R
import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import com.umairshahab.etea.studyplan.domain.Subject
import com.umairshahab.etea.studyplan.ui.components.EmptyStateView
import com.umairshahab.etea.studyplan.ui.components.GradientPillButton
import com.umairshahab.etea.studyplan.ui.components.GradientPillChip
import com.umairshahab.etea.studyplan.ui.components.HorizontalMonthCalendar
import com.umairshahab.etea.studyplan.ui.components.MetricCard
import com.umairshahab.etea.studyplan.ui.components.RevisionRowItem
import com.umairshahab.etea.studyplan.ui.components.SettingsSheet
import com.umairshahab.etea.studyplan.ui.components.StaggeredCardEntrance
import com.umairshahab.etea.studyplan.ui.components.TopicRowItem
import com.umairshahab.etea.studyplan.ui.components.formatChapterSubtitle
import com.umairshahab.etea.studyplan.ui.theme.Motion
import com.umairshahab.etea.studyplan.ui.theme.PrimaryGradientBrush
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults
import com.umairshahab.etea.studyplan.ui.theme.ThemeMode

@Composable
fun HomeScreen(
    topics: List<TopicEntity>,
    revisions: List<RevisionEntity>,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAddTopic: () -> Unit,
    onMarkDone: (Long) -> Unit,
    onNavigateToTopics: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onEnableBackgroundAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("study_plan_prefs", Context.MODE_PRIVATE)
    }
    var onboardingDismissed by remember {
        mutableStateOf(prefs.getBoolean("onboarding_dismissed", false))
    }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val dueTodayCount = revisions.count {
        it.status == "SCHEDULED" && RevisionScheduler.isSameDay(it.dueAt, now) && it.dueAt >= now
    }
    val missedCount = revisions.count {
        it.status == "SCHEDULED" && it.dueAt < now
    }

    val glassColors = StudyPlanThemeDefaults.glassColors

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SP",
                        style = TextStyle(
                            brush = PrimaryGradientBrush,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Study Plan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Spaced Repetition Engine",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier.semantics {
                        contentDescription = "Open Settings"
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StaggeredCardEntrance(
                    index = 0,
                    modifier = Modifier.weight(1f)
                ) {
                    MetricCard(
                        label = "Topics",
                        count = topics.size,
                        contentColor = if (glassColors.isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                    )
                }
                StaggeredCardEntrance(
                    index = 1,
                    modifier = Modifier.weight(1f)
                ) {
                    MetricCard(
                        label = "Today",
                        count = dueTodayCount,
                        contentColor = if (glassColors.isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
                    )
                }
                StaggeredCardEntrance(
                    index = 2,
                    modifier = Modifier.weight(1f)
                ) {
                    MetricCard(
                        label = "Missed",
                        count = missedCount,
                        contentColor = if (glassColors.isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                    )
                }
            }
        }

        // Action & Counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total ${topics.size}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }

        // Horizontal Month-Grid Calendar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Revision Schedule",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalMonthCalendar(
                    revisions = revisions,
                    topics = topics,
                    onMarkDone = onMarkDone
                )
            }
        }

        // Onboarding card (when topics == 0 and not dismissed) or Empty state
        if (topics.isEmpty()) {
            if (!onboardingDismissed) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
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
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Welcome to Study Plan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Master your curriculum using scientific spaced repetition in three easy steps:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(brush = PrimaryGradientBrush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "1",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Add topics",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(brush = PrimaryGradientBrush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "2",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Set intervals & time",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(brush = PrimaryGradientBrush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "3",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Get reminders",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                GradientPillButton(
                                    text = "Got it",
                                    onClick = {
                                        prefs.edit().putBoolean("onboarding_dismissed", true).apply()
                                        onboardingDismissed = true
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    EmptyStateView(
                        iconRes = R.drawable.ic_empty_book,
                        title = "No topics yet",
                        message = "Start your study journey by tapping + Add Topic above. Choose your subject, title, revision time, and intervals.",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            // Next up glass card (replaces Recent Topics list)
            item {
                val nextUpRevision = revisions
                    .filter { it.status == "SCHEDULED" && it.dueAt >= now }
                    .minByOrNull { it.dueAt }
                val nextUpTopic = nextUpRevision?.let { rev -> topics.find { it.id == rev.topicId } }

                StaggeredCardEntrance(
                    index = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Next up",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "View all",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(onClick = onNavigateToTopics)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (nextUpRevision != null && nextUpTopic != null) {
                                Text(
                                    text = nextUpTopic.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val subtitle = formatChapterSubtitle(nextUpTopic.chapter, nextUpTopic.subject)
                                Text(
                                    text = subtitle,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Due: ${RevisionScheduler.format(nextUpRevision.dueAt)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "No upcoming revisions",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "All scheduled revisions are completed.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSettingsSheet) {
        SettingsSheet(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onExportBackup = onExportBackup,
            onImportBackup = onImportBackup,
            onEnableBackgroundAlerts = onEnableBackgroundAlerts,
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviseScreen(
    topics: List<TopicEntity>,
    revisions: List<RevisionEntity>,
    onMarkDone: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val topicMap = remember(topics) { topics.associateBy { it.id } }
    val isDark = StudyPlanThemeDefaults.glassColors.isDark

    val dueToday = remember(revisions, now) {
        revisions.filter {
            it.status == "SCHEDULED" && RevisionScheduler.isSameDay(it.dueAt, now) && it.dueAt >= now
        }
    }

    val missed = remember(revisions, now) {
        revisions.filter {
            it.status == "SCHEDULED" && it.dueAt < now
        }
    }

    val upcoming = remember(revisions, now) {
        revisions.filter {
            it.status == "SCHEDULED" && it.dueAt >= now && !RevisionScheduler.isSameDay(it.dueAt, now)
        }.take(10)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Revision Queue",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Static spaced repetition schedule",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Empty state when nothing is due today and nothing missed
        if (dueToday.isEmpty() && missed.isEmpty()) {
            item {
                val nextScheduled = remember(revisions, now) {
                    revisions.filter { it.status == "SCHEDULED" && it.dueAt >= now }.minByOrNull { it.dueAt }
                }
                val nextTopic = nextScheduled?.let { topicMap[it.topicId] }
                val nextMessage = if (nextScheduled != null && nextTopic != null) {
                    "Next: ${RevisionScheduler.format(nextScheduled.dueAt)} – ${nextTopic.title}"
                } else {
                    "All scheduled revisions are completed."
                }

                EmptyStateView(
                    iconRes = R.drawable.ic_empty_clock,
                    title = "Nothing due now.",
                    message = nextMessage
                )
            }
        }

        if (dueToday.isNotEmpty()) {
            item {
                Text(
                    text = "Due Today (${dueToday.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
                )
            }
            itemsIndexed(dueToday, key = { _, it -> it.id }) { index, rev ->
                val topic = topicMap[rev.topicId]
                Box(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                    )
                ) {
                    StaggeredCardEntrance(index = index) {
                        RevisionRowItem(
                            topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                            subject = topic?.subject ?: "",
                            formattedDue = RevisionScheduler.format(rev.dueAt),
                            isMissed = false,
                            onDone = { onMarkDone(rev.id) }
                        )
                    }
                }
            }
        }

        if (missed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Missed Revisions (${missed.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
                )
            }
            itemsIndexed(missed, key = { _, it -> it.id }) { index, rev ->
                val topic = topicMap[rev.topicId]
                Box(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                    )
                ) {
                    StaggeredCardEntrance(index = index) {
                        RevisionRowItem(
                            topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                            subject = topic?.subject ?: "",
                            formattedDue = RevisionScheduler.format(rev.dueAt),
                            isMissed = true,
                            onDone = { onMarkDone(rev.id) }
                        )
                    }
                }
            }
        }

        if (upcoming.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upcoming (Next 10)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            itemsIndexed(upcoming, key = { _, it -> it.id }) { index, rev ->
                val topic = topicMap[rev.topicId]
                Box(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                    )
                ) {
                    StaggeredCardEntrance(index = index) {
                        RevisionRowItem(
                            topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                            subject = topic?.subject ?: "",
                            formattedDue = RevisionScheduler.format(rev.dueAt),
                            isMissed = false,
                            onDone = { onMarkDone(rev.id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectsScreen(
    topics: List<TopicEntity>,
    revisions: List<RevisionEntity>,
    selectedSubject: Subject = Subject.Maths,
    onSubjectChange: (Subject) -> Unit = {},
    onEditTopic: (TopicEntity) -> Unit,
    onDeleteTopic: (Long) -> Unit,
    onAddTopicForSubject: (Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()

    val filteredTopics = remember(topics, selectedSubject) {
        topics.filter { it.subject.equals(selectedSubject.displayName, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subjects",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Filter by fixed subject curriculum",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Horizontal Subject Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Subject.entries.forEach { subj ->
                    GradientPillChip(
                        text = subj.displayName,
                        selected = subj == selectedSubject,
                        onClick = { onSubjectChange(subj) }
                    )
                }
            }
        }

        if (filteredTopics.isEmpty()) {
            item {
                StaggeredCardEntrance(index = 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyStateView(
                            iconRes = R.drawable.ic_empty_book,
                            title = "No ${selectedSubject.displayName} topics",
                            message = "Add a topic to begin spaced repetition for this subject."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GradientPillButton(
                            text = "Add your first ${selectedSubject.displayName} topic",
                            onClick = { onAddTopicForSubject(selectedSubject) }
                        )
                    }
                }
            }
        } else {
            itemsIndexed(filteredTopics, key = { _, topic -> topic.id }) { index, topic ->
                val nextRev = revisions.firstOrNull { it.topicId == topic.id && it.status == "SCHEDULED" && it.dueAt >= now }
                val nextDueFormatted = nextRev?.let { RevisionScheduler.format(it.dueAt) }
                Box(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                    )
                ) {
                    StaggeredCardEntrance(index = index) {
                        TopicRowItem(
                            topic = topic,
                            nextDueFormatted = nextDueFormatted,
                            onEdit = { onEditTopic(topic) },
                            onDelete = { onDeleteTopic(topic.id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllTopicsScreen(
    topics: List<TopicEntity>,
    revisions: List<RevisionEntity>,
    onEditTopic: (TopicEntity) -> Unit,
    onDeleteTopic: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    var searchQuery by remember { mutableStateOf("") }

    val filteredTopics = remember(topics, searchQuery) {
        if (searchQuery.isBlank()) {
            topics
        } else {
            val query = searchQuery.trim()
            topics.filter {
                it.title.contains(query, ignoreCase = true) ||
                    (it.chapter?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    val isDark = StudyPlanThemeDefaults.glassColors.isDark

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Topics (${topics.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Full study repository and revision targets",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search topics or chapters...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = StudyPlanThemeDefaults.glassColors.cardSurface,
                    unfocusedContainerColor = StudyPlanThemeDefaults.glassColors.cardSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (topics.isEmpty()) {
            item {
                EmptyStateView(
                    iconRes = R.drawable.ic_empty_list,
                    title = "No topics created",
                    message = "All created topics across Maths, Physics, Chemistry, and English appear here."
                )
            }
        } else if (filteredTopics.isEmpty()) {
            item {
                EmptyStateView(
                    iconRes = R.drawable.ic_empty_search,
                    title = "No matching topics",
                    message = "No topics or chapters found matching \"$searchQuery\"."
                )
            }
        } else {
            itemsIndexed(filteredTopics, key = { _, topic -> topic.id }) { index, topic ->
                val nextRev = revisions.firstOrNull { it.topicId == topic.id && it.status == "SCHEDULED" && it.dueAt >= now }
                val nextDueFormatted = nextRev?.let { RevisionScheduler.format(it.dueAt) }
                Box(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(durationMillis = Motion.FAST, easing = Motion.STANDARD)
                    )
                ) {
                    StaggeredCardEntrance(index = index) {
                        TopicRowItem(
                            topic = topic,
                            nextDueFormatted = nextDueFormatted,
                            onEdit = { onEditTopic(topic) },
                            onDelete = { onDeleteTopic(topic.id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
