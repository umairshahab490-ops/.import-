package com.umairshahab.etea.studyplan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val subject: String,
    val title: String,
    val chapter: String?,
    val createdAt: Long,
    val revisionHour: Int,
    val revisionMinute: Int,
    val intervals: List<Int>
)

@Entity(tableName = "revisions")
data class RevisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val topicId: Long,
    val intervalIndex: Int,
    val intervalDays: Int,
    val dueAt: Long,
    val alertAt: Long,
    val status: String, // SCHEDULED / DONE / MISSED
    val completedAt: Long?
)
