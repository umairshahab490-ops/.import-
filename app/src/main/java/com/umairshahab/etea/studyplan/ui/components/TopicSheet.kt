package com.umairshahab.etea.studyplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import com.umairshahab.etea.studyplan.domain.Subject
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSheet(
    topicToEdit: TopicEntity?,
    initialSubject: Subject = Subject.Maths,
    onDismiss: () -> Unit,
    onSave: (
        subject: String,
        title: String,
        chapter: String?,
        hour: Int,
        minute: Int,
        intervals: List<Int>
    ) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedSubject by remember(topicToEdit, initialSubject) {
        mutableStateOf(
            topicToEdit?.let { Subject.fromName(it.subject) } ?: initialSubject
        )
    }
    var title by remember(topicToEdit) { mutableStateOf(topicToEdit?.title ?: "") }
    var chapter by remember(topicToEdit) { mutableStateOf(topicToEdit?.chapter ?: "") }
    var intervalsText by remember(topicToEdit) {
        mutableStateOf(
            topicToEdit?.intervals?.joinToString(",")
                ?: RevisionScheduler.DEFAULT_INTERVALS.joinToString(",")
        )
    }

    val initialHour = topicToEdit?.revisionHour ?: 18
    val initialMinute = topicToEdit?.revisionMinute ?: 30

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    val parsedIntervals by remember(intervalsText) {
        derivedStateOf { RevisionScheduler.parseIntervals(intervalsText) }
    }

    val previewList by remember(timePickerState.hour, timePickerState.minute, parsedIntervals) {
        derivedStateOf {
            if (parsedIntervals.isEmpty()) emptyList()
            else RevisionScheduler.previewTimestamps(
                nowMillis = System.currentTimeMillis(),
                hour = timePickerState.hour,
                minute = timePickerState.minute,
                intervals = parsedIntervals
            )
        }
    }

    val isSaveEnabled = title.isNotBlank() && parsedIntervals.isNotEmpty()
    val scrollState = rememberScrollState()
    val isDark = StudyPlanThemeDefaults.glassColors.isDark

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0B1329) else Color(0xFFF8FAFC),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = if (topicToEdit == null) "Add Study Topic" else "Edit Study Topic",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Subject chips
            Text(
                text = "Subject",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
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
                Spacer(modifier = Modifier.width(16.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Topic Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chapter
            OutlinedTextField(
                value = chapter,
                onValueChange = { chapter = it },
                label = { Text("Chapter (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Revision Time
            Text(
                text = "Daily Revision Alert Time (24h)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Intervals
            OutlinedTextField(
                value = intervalsText,
                onValueChange = { intervalsText = it },
                label = { Text("Revision Intervals (days, comma-separated)") },
                supportingText = { Text("Default: 3,7,14,21,30,45,60,90,120,180,365") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live preview
            Text(
                text = "Scheduled Revision Previews",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (previewList.isEmpty()) {
                Text(
                    text = "Enter valid interval numbers to generate schedule",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val displayCount = previewList.take(6)
                    displayCount.forEachIndexed { idx, timestamp ->
                        val dayInterval = parsedIntervals.getOrNull(idx) ?: 0
                        Text(
                            text = "• Revision ${idx + 1} (+${dayInterval}d): ${RevisionScheduler.format(timestamp)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (previewList.size > 6) {
                        val remaining = previewList.size - 6
                        Text(
                            text = "...and $remaining more scheduled revisions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save pill button
            GradientPillButton(
                text = if (topicToEdit == null) "Save Topic" else "Update Topic",
                onClick = {
                    if (isSaveEnabled) {
                        onSave(
                            selectedSubject.displayName,
                            title,
                            chapter,
                            timePickerState.hour,
                            timePickerState.minute,
                            parsedIntervals
                        )
                    }
                },
                enabled = isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
