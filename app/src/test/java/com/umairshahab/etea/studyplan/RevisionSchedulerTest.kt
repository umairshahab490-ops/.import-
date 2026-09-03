package com.umairshahab.etea.studyplan

import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class RevisionSchedulerTest {

    private val zoneId: ZoneId = ZoneId.of("UTC")

    @Test
    fun defaultIntervalsGenerateExactlyFiveRevisionsWithCorrectDueDates() {
        val topicId = 1L
        val requestedHour = 10
        val requestedMinute = 30

        // Fixed anchor at 2026-09-01 08:00 UTC
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, requestedHour, requestedMinute, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = RevisionScheduler.DEFAULT_INTERVALS,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        assertEquals(5, revisions.size)
        assertEquals(listOf(1, 3, 7, 15, 30), RevisionScheduler.DEFAULT_INTERVALS)

        val expectedDays = listOf(1, 3, 7, 15, 30)
        val baseZdt = Instant.ofEpochMilli(baseMillis).atZone(zoneId)

        revisions.forEachIndexed { index, revision ->
            assertEquals(expectedDays[index], revision.intervalDays)
            assertEquals(index, revision.intervalIndex)
            val expectedDueMillis = baseZdt.plusDays(expectedDays[index].toLong()).toInstant().toEpochMilli()
            assertEquals(expectedDueMillis, revision.dueAt)
        }
    }

    @Test
    fun dueDatesLandAtRequestedHourAndMinute() {
        val topicId = 2L
        val requestedHour = 16
        val requestedMinute = 45

        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 10, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, requestedHour, requestedMinute, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = RevisionScheduler.DEFAULT_INTERVALS,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        assertTrue(revisions.isNotEmpty())
        for (revision in revisions) {
            val dueZdt = Instant.ofEpochMilli(revision.dueAt).atZone(zoneId)
            assertEquals(requestedHour, dueZdt.hour)
            assertEquals(requestedMinute, dueZdt.minute)
        }
    }

    @Test
    fun notificationAlertOffsetIsFifteenMinutesBefore() {
        val topicId = 3L
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, 9, 0, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = RevisionScheduler.DEFAULT_INTERVALS,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        val expectedOffsetMillis = 15 * 60 * 1000L // 15 min
        revisions.forEach { revision ->
            assertEquals(expectedOffsetMillis, revision.dueAt - revision.alertAt)
        }
    }

    @Test
    fun customIntervalsProduceMatchingCountAndDays() {
        val topicId = 4L
        val customIntervals = listOf(2, 5, 10, 20)
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, 11, 0, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = customIntervals,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        assertEquals(4, revisions.size)
        val expectedDays = listOf(2, 5, 10, 20)
        revisions.forEachIndexed { index, revision ->
            assertEquals(expectedDays[index], revision.intervalDays)
            assertEquals(index, revision.intervalIndex)
        }
    }
}
