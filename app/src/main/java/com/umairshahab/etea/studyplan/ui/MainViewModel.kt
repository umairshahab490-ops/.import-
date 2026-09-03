package com.umairshahab.etea.studyplan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.umairshahab.etea.studyplan.data.local.RevisionDao
import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicDao
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val topicDao: TopicDao,
    private val revisionDao: RevisionDao
) : ViewModel() {

    val topics: StateFlow<List<TopicEntity>> = topicDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val revisions: StateFlow<List<RevisionEntity>> = revisionDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addTopic(
        subject: String,
        title: String,
        chapter: String?,
        revisionHour: Int,
        revisionMinute: Int,
        intervals: List<Int>
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val topic = TopicEntity(
                subject = subject,
                title = title.trim(),
                chapter = chapter?.trim()?.ifBlank { null },
                createdAt = now,
                revisionHour = revisionHour,
                revisionMinute = revisionMinute,
                intervals = intervals
            )
            val topicId = topicDao.insert(topic)
            val baseTimestamp = RevisionScheduler.baseTimestamp(now, revisionHour, revisionMinute)
            val futureRevisions = RevisionScheduler.buildRevisions(topicId, baseTimestamp, intervals, now)
            if (futureRevisions.isNotEmpty()) {
                revisionDao.insertAll(futureRevisions)
            }
        }
    }

    fun updateTopic(
        topicId: Long,
        subject: String,
        title: String,
        chapter: String?,
        revisionHour: Int,
        revisionMinute: Int,
        intervals: List<Int>
    ) {
        viewModelScope.launch {
            val existing = topics.value.find { it.id == topicId } ?: return@launch
            val updated = existing.copy(
                subject = subject,
                title = title.trim(),
                chapter = chapter?.trim()?.ifBlank { null },
                revisionHour = revisionHour,
                revisionMinute = revisionMinute,
                intervals = intervals
            )
            topicDao.update(updated)

            // Keep completed revision history, delete only SCHEDULED revisions
            revisionDao.deleteScheduledForTopic(topicId)

            val now = System.currentTimeMillis()
            // Regenerate future revisions from the ORIGINAL createdAt base with the new settings
            val baseTimestamp = RevisionScheduler.baseTimestamp(existing.createdAt, revisionHour, revisionMinute)
            val futureRevisions = RevisionScheduler.buildRevisions(topicId, baseTimestamp, intervals, now)
            if (futureRevisions.isNotEmpty()) {
                revisionDao.insertAll(futureRevisions)
            }
        }
    }

    fun deleteTopic(topicId: Long) {
        viewModelScope.launch {
            // Delete the topic and ALL its revisions
            revisionDao.deleteAllForTopic(topicId)
            topicDao.deleteById(topicId)
        }
    }

    fun markDone(revisionId: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            revisionDao.updateStatus(revisionId, "DONE", now)
        }
    }

    class Factory(
        private val topicDao: TopicDao,
        private val revisionDao: RevisionDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(topicDao, revisionDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
