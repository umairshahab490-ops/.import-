package com.umairshahab.etea.studyplan.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import com.umairshahab.etea.studyplan.domain.Subject
import com.umairshahab.etea.studyplan.ui.components.EmptyStateView
import com.umairshahab.etea.studyplan.ui.components.MetricCard
import com.umairshahab.etea.studyplan.ui.components.PlaceholderCalendarCard
import com.umairshahab.etea.studyplan.ui.components.RevisionRowItem
import com.umairshahab.etea.studyplan.ui.components.TopicRowItem

@Composable
fun HomeScreen(
    topics: List<TopicEntity>,
    revisions: List<RevisionEntity>,
    onAddTopic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val dueTodayCount = revisions.count {
        it.status == "SCHEDULED" && RevisionScheduler.isSameDay(it.dueAt, now) && it.dueAt >= now
    }
    val missedCount = revisions.count {
        it.status == "SCHEDULED" && it.dueAt < now
    }

    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SP",
                    style = TextStyle(
                        brush = gradientBrush,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Study Plan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = "Topics",
                    count = topics.size,
                    containerColor = Color(0xFFEFF6FF),
                    contentColor = Color(0xFF1D4ED8),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Today",
                    count = dueTodayCount,
                    containerColor = Color(0xFFF0FDF4),
                    contentColor = Color(0xFF15803D),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Missed",
                    count = missedCount,
                    containerColor = Color(0xFFFEF2F2),
                    contentColor = Color(0xFFB91C1C),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action & Counter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total ${topics.size}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onAddTopic,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    )
                ) {
                    Text("+ Add Topic", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Calendar Placeholder Card
        item {
            PlaceholderCalendarCard()
        }

        if (topics.isEmpty()) {
            item {
                EmptyStateView(
                    emoji = "📚",
                    title = "No topics yet",
                    message = "Start your study journey by tapping + Add Topic above. Choose your subject, title, revision time, and intervals."
                )
            }
        } else {
            item {
                Text(
                    text = "Recent Topics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(topics.take(5), key = { it.id }) { topic ->
                val nextRev = revisions.firstOrNull { it.topicId == topic.id && it.status == "SCHEDULED" && it.dueAt >= now }
                val nextDueFormatted = nextRev?.let { RevisionScheduler.format(it.dueAt) }
                TopicRowItem(
                    topic = topic,
                    nextDueFormatted = nextDueFormatted,
                    onEdit = {},
                    onDelete = {}
                )
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
                    color = Color(0xFF15803D)
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
                    color = Color(0xFFB91C1C)
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
                Button(
                    onClick = { onAddTopicForSubject(selectedSubject) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    )
                ) {
                    Text("+ Add", fontWeight = FontWeight.Bold)
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
                    val isSelected = subj == selectedSubject
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubject = subj },
                        label = { Text(subj.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
        } else {
            items(topics, key = { it.id }) { topic ->
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
