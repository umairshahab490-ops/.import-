package com.umairshahab.etea.studyplan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.umairshahab.etea.studyplan.data.local.AppDatabase
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.Subject
import com.umairshahab.etea.studyplan.notifications.NotificationHelper
import com.umairshahab.etea.studyplan.notifications.ReminderWorker
import com.umairshahab.etea.studyplan.ui.AllTopicsScreen
import com.umairshahab.etea.studyplan.ui.HomeScreen
import com.umairshahab.etea.studyplan.ui.MainViewModel
import com.umairshahab.etea.studyplan.ui.ReviseScreen
import com.umairshahab.etea.studyplan.ui.SubjectsScreen
import com.umairshahab.etea.studyplan.ui.components.TopicSheet
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getInstance(applicationContext) }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application, db.topicDao(), db.revisionDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize notification channels
        NotificationHelper.createChannels(applicationContext)

        // 2. Schedule periodic WorkManager for missed checking & alert scheduling (~15 mins)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            15, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "study_plan_reminder_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

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
    val context = LocalContext.current
    val topics by viewModel.topics.collectAsState()
    val revisions by viewModel.revisions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicEntity?>(null) }
    var defaultSubjectForNew by remember { mutableStateOf(Subject.Maths) }

    // Permission launcher for Android 13+ (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // App remains fully functional whether granted or denied
    }

    fun requestNotificationPermissionIfFirstTopic() {
        if (topics.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun handleDeleteTopic(topicId: Long) {
        viewModel.deleteTopic(topicId) { deletedTopic ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Deleted \"${deletedTopic.title}\"",
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                }
            }
        }
    }

    val navItems = listOf(
        Pair("Home", "🏠"),
        Pair("Revise", "⏰"),
        Pair("Subjects", "📚"),
        Pair("All", "📋")
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Text(
                                text = item.second,
                                fontSize = 20.sp,
                                modifier = Modifier.semantics {
                                    contentDescription = "${item.first} tab"
                                }
                            )
                        },
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
                    handleDeleteTopic(topicId)
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
                    handleDeleteTopic(topicId)
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
                    requestNotificationPermissionIfFirstTopic()
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
}
