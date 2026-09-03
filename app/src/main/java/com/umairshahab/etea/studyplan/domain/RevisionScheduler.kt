package com.umairshahab.etea.studyplan.domain

import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object RevisionScheduler {
    const val ALERT_OFFSET_MILLIS: Long = 15 * 60 * 1000L // 15 minutes before
    val DEFAULT_INTERVALS: List<Int> = listOf(1, 3, 7, 15, 30)
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)

    fun parseIntervals(text: String): List<Int> {
        return text.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it > 0 }
    }

    fun baseTimestamp(
        anchorMillis: Long,
        hour: Int,
        minute: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val anchorZdt = Instant.ofEpochMilli(anchorMillis).atZone(zoneId)
        val targetTime = LocalTime.of(hour, minute)
        var candidateZdt = anchorZdt.toLocalDate().atTime(targetTime).atZone(zoneId)

        if (!candidateZdt.toInstant().isAfter(Instant.ofEpochMilli(anchorMillis))) {
            candidateZdt = candidateZdt.plusDays(1)
        }
        return candidateZdt.toInstant().toEpochMilli()
    }

    fun buildRevisions(
        topicId: Long,
        baseMillis: Long,
        intervals: List<Int>,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<RevisionEntity> {
        val baseZdt = Instant.ofEpochMilli(baseMillis).atZone(zoneId)
        val revisions = mutableListOf<RevisionEntity>()

        intervals.forEachIndexed { index, days ->
            val dueZdt = baseZdt.plusDays(days.toLong())
            val dueMillis = dueZdt.toInstant().toEpochMilli()
            if (dueMillis > nowMillis) {
                val alertMillis = dueMillis - ALERT_OFFSET_MILLIS
                revisions.add(
                    RevisionEntity(
                        topicId = topicId,
                        intervalIndex = index,
                        intervalDays = days,
                        dueAt = dueMillis,
                        alertAt = alertMillis,
                        status = "SCHEDULED",
                        completedAt = null
                    )
                )
            }
        }
        return revisions
    }

    fun previewTimestamps(
        nowMillis: Long,
        hour: Int,
        minute: Int,
        intervals: List<Int>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Long> {
        val base = baseTimestamp(nowMillis, hour, minute, zoneId)
        val baseZdt = Instant.ofEpochMilli(base).atZone(zoneId)
        return intervals.map { days ->
            baseZdt.plusDays(days.toLong()).toInstant().toEpochMilli()
        }
    }

    fun format(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val zdt = Instant.ofEpochMilli(millis).atZone(zoneId)
        return formatter.format(zdt)
    }

    fun isSameDay(aMillis: Long, bMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
        val aDate: LocalDate = Instant.ofEpochMilli(aMillis).atZone(zoneId).toLocalDate()
        val bDate: LocalDate = Instant.ofEpochMilli(bMillis).atZone(zoneId).toLocalDate()
        return aDate == bDate
    }
}
