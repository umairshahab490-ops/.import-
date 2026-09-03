package com.umairshahab.etea.studyplan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getById(id: Long): TopicEntity?

    @Query("SELECT * FROM topics")
    suspend fun getAll(): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity): Long

    @Update
    suspend fun update(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM topics")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(topics: List<TopicEntity>)
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revisions ORDER BY dueAt ASC")
    fun observeAll(): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE topicId = :topicId")
    suspend fun getForTopic(topicId: Long): List<RevisionEntity>

    @Query("SELECT * FROM revisions WHERE status = 'SCHEDULED' AND dueAt < :nowMillis")
    suspend fun getScheduledPastDue(nowMillis: Long): List<RevisionEntity>

    @Query("SELECT * FROM revisions WHERE status = 'SCHEDULED' AND alertAt BETWEEN :startMillis AND :endMillis")
    suspend fun getScheduledWithAlertBetween(startMillis: Long, endMillis: Long): List<RevisionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(revisions: List<RevisionEntity>)

    @Query("DELETE FROM revisions WHERE topicId = :topicId AND status = 'SCHEDULED'")
    suspend fun deleteScheduledForTopic(topicId: Long)

    @Query("DELETE FROM revisions WHERE topicId = :topicId")
    suspend fun deleteAllForTopic(topicId: Long)

    @Query("DELETE FROM revisions")
    suspend fun deleteAll()

    @Query("UPDATE revisions SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?)
}
