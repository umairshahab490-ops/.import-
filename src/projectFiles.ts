export interface ProjectFile {
  path: string;
  category: 'config' | 'gradle' | 'manifest' | 'kotlin' | 'res' | 'workflow';
  content: string;
  description: string;
}

export const PROJECT_FILES: ProjectFile[] = [
  {
    path: 'settings.gradle.kts',
    category: 'gradle',
    description: 'Gradle repository management and subproject inclusions',
    content: `pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "etea-study-plan"
include(":app")`
  },
  {
    path: 'build.gradle.kts',
    category: 'gradle',
    description: 'Root Gradle build script pinning AGP 8.2.2, Kotlin 1.9.22, and KSP 1.9.22-1.0.17',
    content: `plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}`
  },
  {
    path: 'gradle.properties',
    category: 'config',
    description: 'Gradle daemon, JVM args, and AndroidX flags',
    content: `org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official`
  },
  {
    path: 'gradle/wrapper/gradle-wrapper.properties',
    category: 'gradle',
    description: 'Gradle Wrapper 8.4 bin distribution config',
    content: `distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.4-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists`
  },
  {
    path: 'app/build.gradle.kts',
    category: 'gradle',
    description: 'App module build script: SDK 34, desugaring, Room 2.6.1, Compose BOM 2024.02.00',
    content: `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.umairshahab.etea.studyplan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.umairshahab.etea.studyplan"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ""
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}`
  },
  {
    path: 'app/proguard-rules.pro',
    category: 'config',
    description: 'Proguard keep rules for annotations and desugared java.time',
    content: `# Proguard rules for Study Plan
-keepattributes *Annotation*
-dontwarn java.time.**`
  },
  {
    path: 'app/src/main/AndroidManifest.xml',
    category: 'manifest',
    description: 'Manifest without INTERNET permission, fully offline-first',
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@drawable/ic_launcher"
        android:supportsRtl="true"
        android:theme="@style/Theme.StudyPlan">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.StudyPlan">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/MainActivity.kt',
    category: 'kotlin',
    description: 'Main activity with 4-tab bottom navigation and lazy Room database',
    content: `package com.umairshahab.etea.studyplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.data.local.AppDatabase
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.Subject
import com.umairshahab.etea.studyplan.ui.AllTopicsScreen
import com.umairshahab.etea.studyplan.ui.HomeScreen
import com.umairshahab.etea.studyplan.ui.MainViewModel
import com.umairshahab.etea.studyplan.ui.ReviseScreen
import com.umairshahab.etea.studyplan.ui.SubjectsScreen
import com.umairshahab.etea.studyplan.ui.components.TopicSheet

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getInstance(applicationContext) }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(db.topicDao(), db.revisionDao())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyPlanApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanApp(viewModel: MainViewModel) {
    val topics by viewModel.topics.collectAsState()
    val revisions by viewModel.revisions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicEntity?>(null) }
    var defaultSubjectForNew by remember { mutableStateOf(Subject.Maths) }

    val navItems = listOf(
        Pair("Home", "🏠"),
        Pair("Revise", "⏰"),
        Pair("Subjects", "📚"),
        Pair("All", "📋")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(text = item.second, fontSize = 20.sp) },
                        label = { Text(text = item.first) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        when (selectedTab) {
            0 -> HomeScreen(
                topics = topics,
                revisions = revisions,
                onAddTopic = {
                    topicToEdit = null
                    defaultSubjectForNew = Subject.Maths
                    showSheet = true
                },
                modifier = modifier
            )
            1 -> ReviseScreen(
                topics = topics,
                revisions = revisions,
                onMarkDone = { revId -> viewModel.markDone(revId) },
                modifier = modifier
            )
            2 -> SubjectsScreen(
                topics = topics,
                revisions = revisions,
                onEditTopic = { topic ->
                    topicToEdit = topic
                    showSheet = true
                },
                onDeleteTopic = { topicId ->
                    viewModel.deleteTopic(topicId)
                },
                onAddTopicForSubject = { subject ->
                    topicToEdit = null
                    defaultSubjectForNew = subject
                    showSheet = true
                },
                modifier = modifier
            )
            3 -> AllTopicsScreen(
                topics = topics,
                revisions = revisions,
                onEditTopic = { topic ->
                    topicToEdit = topic
                    showSheet = true
                },
                onDeleteTopic = { topicId ->
                    viewModel.deleteTopic(topicId)
                },
                modifier = modifier
            )
        }

        if (showSheet) {
            TopicSheet(
                topicToEdit = topicToEdit,
                initialSubject = defaultSubjectForNew,
                onDismiss = {
                    showSheet = false
                    topicToEdit = null
                },
                onSave = { subject, title, chapter, hour, minute, intervals ->
                    if (topicToEdit == null) {
                        viewModel.addTopic(subject, title, chapter, hour, minute, intervals)
                    } else {
                        viewModel.updateTopic(topicToEdit!!.id, subject, title, chapter, hour, minute, intervals)
                    }
                    showSheet = false
                    topicToEdit = null
                }
            )
        }
    }
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/domain/Subject.kt',
    category: 'kotlin',
    description: 'Fixed constant subject enum: Maths, Physics, Chemistry, English',
    content: `package com.umairshahab.etea.studyplan.domain

enum class Subject(val displayName: String) {
    Maths("Maths"),
    Physics("Physics"),
    Chemistry("Chemistry"),
    English("English");

    companion object {
        fun fromName(name: String): Subject {
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
            } ?: Maths
        }
    }
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/domain/RevisionScheduler.kt',
    category: 'kotlin',
    description: 'Pure static spaced revision scheduler using java.time with desugaring',
    content: `package com.umairshahab.etea.studyplan.domain

import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object RevisionScheduler {
    val DEFAULT_INTERVALS: List<Int> = listOf(3, 7, 14, 21, 30, 45, 60, 90, 120, 180, 365)
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
                val alertMillis = dueMillis - 120000L
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
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/data/local/Entities.kt',
    category: 'kotlin',
    description: 'Room entities for topics and revisions',
    content: `package com.umairshahab.etea.studyplan.data.local

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
)`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/data/local/Converters.kt',
    category: 'kotlin',
    description: 'Room TypeConverter for comma-separated List<Int> intervals',
    content: `package com.umairshahab.etea.studyplan.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/data/local/Daos.kt',
    category: 'kotlin',
    description: 'Room DAOs for topics and revisions',
    content: `package com.umairshahab.etea.studyplan.data.local

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity): Long

    @Update
    suspend fun update(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revisions ORDER BY dueAt ASC")
    fun observeAll(): Flow<List<RevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(revisions: List<RevisionEntity>)

    @Query("DELETE FROM revisions WHERE topicId = :topicId AND status = 'SCHEDULED'")
    suspend fun deleteScheduledForTopic(topicId: Long)

    @Query("DELETE FROM revisions WHERE topicId = :topicId")
    suspend fun deleteAllForTopic(topicId: Long)

    @Query("UPDATE revisions SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?)
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/data/local/AppDatabase.kt',
    category: 'kotlin',
    description: 'Room AppDatabase singleton named etea_blank_v1',
    content: `package com.umairshahab.etea.studyplan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TopicEntity::class, RevisionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun revisionDao(): RevisionDao

    companion object {
        const val DATABASE_NAME = "etea_blank_v1"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/ui/MainViewModel.kt',
    category: 'kotlin',
    description: 'MainViewModel managing topic state, static regeneration, and mark done',
    content: `package com.umairshahab.etea.studyplan.ui

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
            throw IllegalArgumentException("Unknown ViewModel class: \${modelClass.name}")
        }
    }
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/ui/Screens.kt',
    category: 'kotlin',
    description: 'Jetpack Compose screens: Home, Revise, Subjects, and All Topics',
    content: `package com.umairshahab.etea.studyplan.ui

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
                    text = "Total \${topics.size}",
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
                    text = "Due Today (\${dueToday.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
            items(dueToday, key = { it.id }) { rev ->
                val topic = topicMap[rev.topicId]
                RevisionRowItem(
                    topicTitle = topic?.title ?: "Topic #\${rev.topicId}",
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
                    text = "Missed Revisions (\${missed.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB91C1C)
                )
            }
            items(missed, key = { it.id }) { rev ->
                val topic = topicMap[rev.topicId]
                RevisionRowItem(
                    topicTitle = topic?.title ?: "Topic #\${rev.topicId}",
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
                    topicTitle = topic?.title ?: "Topic #\${rev.topicId}",
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
                    title = "No \${selectedSubject.displayName} topics",
                    message = "Tap '+ Add' above to create a topic under \${selectedSubject.displayName}."
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
                text = "All Topics (\${topics.size})",
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
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/ui/components/SharedComponents.kt',
    category: 'kotlin',
    description: 'Shared UI components: MetricCard, PlaceholderCalendarCard, TopicRowItem, RevisionRowItem, EmptyStateView',
    content: `package com.umairshahab.etea.studyplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.data.local.TopicEntity

@Composable
fun MetricCard(
    label: String,
    count: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PlaceholderCalendarCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📅", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Calendar View",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Calendar arrives later. Track daily targets right here!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TopicRowItem(
    topic: TopicEntity,
    nextDueFormatted: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val chapterPart = if (!topic.chapter.isNullOrBlank()) "\${topic.chapter} • " else ""
                    val duePart = nextDueFormatted?.let { "Next due: \$it" } ?: "All revisions completed"
                    Text(
                        text = "\$chapterPart\${topic.subject}\\n\$duePart",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("Edit", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("Delete", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun RevisionRowItem(
    topicTitle: String,
    subject: String,
    formattedDue: String,
    isMissed: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMissed) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = topicTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• \$subject",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isMissed) "Missed: \$formattedDue" else "Due: \$formattedDue",
                    fontSize = 13.sp,
                    color = if (isMissed) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isMissed) FontWeight.Medium else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMissed) Color(0xFFDC2626) else Color(0xFF16A34A),
                    contentColor = Color.White
                )
            ) {
                Text("Done", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyStateView(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}`
  },
  {
    path: 'app/src/main/java/com/umairshahab/etea/studyplan/ui/components/TopicSheet.kt',
    category: 'kotlin',
    description: 'Add and Edit ModalBottomSheet with 24h TimePicker and live revision preview',
    content: `package com.umairshahab.etea.studyplan.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import com.umairshahab.etea.studyplan.domain.Subject

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
    var selectedSubject by remember {
        mutableStateOf(
            topicToEdit?.let { Subject.fromName(it.subject) } ?: initialSubject
        )
    }
    var title by remember { mutableStateOf(topicToEdit?.title ?: "") }
    var chapter by remember { mutableStateOf(topicToEdit?.chapter ?: "") }
    var intervalsText by remember {
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                    val isSelected = subj == selectedSubject
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubject = subj },
                        label = { Text(subj.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
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
                            text = "• Revision \${idx + 1} (+\${dayInterval}d): \${RevisionScheduler.format(timestamp)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (previewList.size > 6) {
                        val remaining = previewList.size - 6
                        Text(
                            text = "...and \$remaining more scheduled revisions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save pill button
            Button(
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
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (topicToEdit == null) "Save Topic" else "Update Topic",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}`
  },
  {
    path: 'app/src/main/res/values/strings.xml',
    category: 'res',
    description: 'App name resource',
    content: `<resources>
    <string name="app_name">Study Plan</string>
</resources>`
  },
  {
    path: 'app/src/main/res/values/colors.xml',
    category: 'res',
    description: 'Color palette definition',
    content: `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#3B82F6</color>
    <color name="primary_variant">#2563EB</color>
    <color name="secondary">#8B5CF6</color>
    <color name="background">#F8FAFC</color>
    <color name="surface">#FFFFFF</color>
    <color name="ic_launcher_background">#3DDC84</color>
</resources>`
  },
  {
    path: 'app/src/main/res/values/themes.xml',
    category: 'res',
    description: 'NoActionBar Material Light theme definition',
    content: `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.StudyPlan" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@color/background</item>
        <item name="android:windowLightStatusBar">true</item>
    </style>
</resources>`
  },
  {
    path: 'app/src/main/res/drawable/ic_launcher.xml',
    category: 'res',
    description: 'Simple vector square icon with #3DDC84',
    content: `<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#3DDC84"
        android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M32,32h44v44h-44z" />
</vector>`
  },
  {
    path: '.github/workflows/android-build.yml',
    category: 'workflow',
    description: 'GitHub Actions workflow to bootstrap Gradle 8.4 and build debug APK',
    content: `name: Android Build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    env:
      FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: 'true'

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v5

      - name: Set up JDK 17
        uses: actions/setup-java@v5
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v6
        with:
          gradle-version: '8.4'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v4

      - name: Bootstrap Gradle Wrapper
        run: |
          gradle wrapper --gradle-version 8.4
          chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew clean assembleDebug --no-daemon

      - name: Upload Debug APK
        uses: actions/upload-artifact@v6
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 7`
  }
];
