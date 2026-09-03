package com.umairshahab.etea.studyplan.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.umairshahab.etea.studyplan.ui.components.TopicRowItem
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
    modifier: Modifier = Modifier
) {
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
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SP",
                        style = TextStyle(
                            brush = PrimaryGradientBrush,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Study Plan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Theme Mode dropdown selector
                Box {
                    var menuExpanded by remember { mutableStateOf(false) }
                    val currentIcon = when (themeMode) {
                        ThemeMode.LIGHT -> "☀️"
                        ThemeMode.DARK -> "🌙"
                        ThemeMode.SYSTEM -> "🔄"
                    }

                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.semantics {
                            contentDescription = "Theme selector, current is ${themeMode.name}"
                        }
                    ) {
                        Text(text = currentIcon, fontSize = 22.sp)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(
                            if (glassColors.isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                        )
                    ) {
                        DropdownMenuItem(
                            text = { Text("☀️ Light") },
                            onClick = {
                                onThemeModeChange(ThemeMode.LIGHT)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🌙 Dark") },
                            onClick = {
                                onThemeModeChange(ThemeMode.DARK)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🔄 System") },
                            onClick = {
                                onThemeModeChange(ThemeMode.SYSTEM)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = "Topics",
                    count = topics.size,
                    containerColor = glassColors.metricTopicsBg,
                    contentColor = glassColors.metricTopicsText,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Today",
                    count = dueTodayCount,
                    containerColor = glassColors.metricTodayBg,
                    contentColor = glassColors.metricTodayText,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Missed",
                    count = missedCount,
                    containerColor = glassColors.metricMissedBg,
                    contentColor = glassColors.metricMissedText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action & Counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total ${topics.size}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                GradientPillButton(
                    text = "+ Add Topic",
                    onClick = onAddTopic
                )
            }
        }

        // Horizontal Month-Grid Calendar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Revision Schedule",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalMonthCalendar(
                    revisions = revisions,
                    topics = topics,
                    onMarkDone = onMarkDone
                )
            }
        }

        if (topics.isEmpty()) {
            item {
                EmptyStateView(
                    emoji = "📚",
                    title = "No topics yet",
                    message = "Start your study journey by tapping + Add Topic above. Choose your subject, title, revision time, and intervals.",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        } else {
            item {
                Text(
                    text = "Recent Topics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            items(topics.take(5), key = { it.id }) { topic ->
                val nextRev = revisions.firstOrNull { it.topicId == topic.id && it.status == "SCHEDULED" && it.dueAt >= now }
                val nextDueFormatted = nextRev?.let { RevisionScheduler.format(it.dueAt) }
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    TopicRowItem(
                        topic = topic,
                        nextDueFormatted = nextDueFormatted,
                        onEdit = {},
                        onDelete = {}
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

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
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Revision Queue",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Static spaced repetition schedule",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (dueToday.isEmpty() && missed.isEmpty() && upcoming.isEmpty()) {
            item {
                EmptyStateView(
                    emoji = "⏰",
                    title = "Nothing to revise",
                    message = "All revisions are completed or no topics have been scheduled yet."
                )
            }
        }

        if (dueToday.isNotEmpty()) {
            item {
                Text(
                    text = "Due Today (${dueToday.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
                )
            }
            items(dueToday, key = { it.id }) { rev ->
                val topic = topicMap[rev.topicId]
                RevisionRowItem(
                    topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                    subject = topic?.subject ?: "",
                    formattedDue = RevisionScheduler.format(rev.dueAt),
                    isMissed = false,
                    onDone = { onMarkDone(rev.id) }
                )
            }
        }

        if (missed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Missed Revisions (${missed.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
                )
            }
            items(missed, key = { it.id }) { rev ->
                val topic = topicMap[rev.topicId]
                RevisionRowItem(
                    topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                    subject = topic?.subject ?: "",
                    formattedDue = RevisionScheduler.format(rev.dueAt),
                    isMissed = true,
                    onDone = { onMarkDone(rev.id) }
                )
            }
        }

        if (upcoming.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upcoming (Next 10)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            items(upcoming, key = { it.id }) { rev ->
                val topic = topicMap[rev.topicId]
                RevisionRowItem(
                    topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                    subject = topic?.subject ?: "",
                    formattedDue = RevisionScheduler.format(rev.dueAt),
                    isMissed = false,
                    onDone = { onMarkDone(rev.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SubjectsScreen(
    topics: List<TopicEntity>,
    revisions: List<RevisionEntity>,
    onEditTopic: (TopicEntity) -> Unit,
    onDeleteTopic: (Long) -> Unit,
    onAddTopicForSubject: (Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by remember { mutableStateOf(Subject.Maths) }
    val now = System.currentTimeMillis()

    val filteredTopics = remember(topics, selectedSubject) {
        topics.filter { it.subject.equals(selectedSubject.displayName, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subjects",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Filter by fixed subject curriculum",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                GradientPillButton(
                    text = "+ Add",
                    onClick = { onAddTopicForSubject(selectedSubject) }
                )
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
                        onClick = { selectedSubject = subj }
                    )
                }
            }
        }

        if (filteredTopics.isEmpty()) {
            item {
                EmptyStateView(
                    emoji = "📖",
                    title = "No ${selectedSubject.displayName} topics",
                    message = "Tap '+ Add' above to create a topic under ${selectedSubject.displayName}."
                )
            }
        } else {
            items(filteredTopics, key = { it.id }) { topic ->
                val nextRev = revisions.firstOrNull { it.topicId == topic.id && it.status == "SCHEDULED" && it.dueAt >= now }
                val nextDueFormatted = nextRev?.let { RevisionScheduler.format(it.dueAt) }
                TopicRowItem(
                    topic = topic,
                    nextDueFormatted = nextDueFormatted,
                    onEdit = { onEditTopic(topic) },
                    onDelete = { onDeleteTopic(topic.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

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
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Topics (${topics.size})",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Full study repository and revision targets",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search topics or chapters...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Text(
                        text = "🔍",
                        fontSize = 16.sp,
                        modifier = Modifier.semantics { contentDescription = "Search" }
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Text(
                                text = "✕",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    emoji = "📋",
                    title = "No topics created",
                    message = "All created topics across Maths, Physics, Chemistry, and English appear here."
                )
            }
        } else if (filteredTopics.isEmpty()) {
            item {
                EmptyStateView(
                    emoji = "🔍",
                    title = "No matching topics",
                    message = "No topics or chapters found matching \"$searchQuery\"."
                )
            }
        } else {
            items(filteredTopics, key = { it.id }) { topic ->
                val nextRev = revisions.firstOrNull { it.topicId == topic.id && it.status == "SCHEDULED" && it.dueAt >= now }
                val nextDueFormatted = nextRev?.let { RevisionScheduler.format(it.dueAt) }
                TopicRowItem(
                    topic = topic,
                    nextDueFormatted = nextDueFormatted,
                    onEdit = { onEditTopic(topic) },
                    onDelete = { onDeleteTopic(topic.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
