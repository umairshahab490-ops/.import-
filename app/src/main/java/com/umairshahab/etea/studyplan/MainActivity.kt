package com.umairshahab.etea.studyplan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanTheme
import com.umairshahab.etea.studyplan.ui.theme.StudyPlanThemeDefaults
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val db by lazy { (application as StudyPlanApp).database }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application, db.topicDao(), db.revisionDao())
    }

    private val targetTab = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

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
            StudyPlanTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .background(StudyPlanThemeDefaults.glassColors.backgroundGradient)
                ) {
                    StudyPlanScreen(
                        viewModel = viewModel,
                        targetTab = targetTab.value,
                        onClearTargetTab = { targetTab.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("open_tab")?.let {
            targetTab.value = it
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanScreen(
    viewModel: MainViewModel,
    targetTab: String? = null,
    onClearTargetTab: () -> Unit = {}
) {
    val context = LocalContext.current
    val topics by viewModel.topics.collectAsState()
    val revisions by viewModel.revisions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicEntity?>(null) }
    var defaultSubjectForNew by remember { mutableStateOf(Subject.Maths) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(targetTab) {
        if (targetTab == "revise") {
            selectedTab = 1
            onClearTargetTab()
        }
    }

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

    fun checkBatteryOptimizationPrompt() {
        val prefs = context.getSharedPreferences("study_plan_prefs", Context.MODE_PRIVATE)
        val alreadyPrompted = prefs.getBoolean("battery_opt_prompted", false)
        if (!alreadyPrompted) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isIgnoring = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            } else {
                true
            }
            if (!isIgnoring) {
                showBatteryOptimizationDialog = true
            }
        }
    }

    fun dismissBatteryOptimizationPrompt() {
        val prefs = context.getSharedPreferences("study_plan_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("battery_opt_prompted", true).apply()
        showBatteryOptimizationDialog = false
    }

    fun openBatteryOptimizationSettings() {
        dismissBatteryOptimizationPrompt()
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (_: Exception) {
                // Fallback silently if intent cannot be handled
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
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val isDark = StudyPlanThemeDefaults.glassColors.isDark
            NavigationBar(
                containerColor = if (isDark) Color(0xFF020617).copy(alpha = 0.88f) else Color(0xFFF8FAFC).copy(alpha = 0.88f)
            ) {
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
                    val isFirstTopic = topics.isEmpty()
                    requestNotificationPermissionIfFirstTopic()
                    if (topicToEdit == null) {
                        viewModel.addTopic(subject, title, chapter, hour, minute, intervals)
                    } else {
                        viewModel.updateTopic(topicToEdit!!.id, subject, title, chapter, hour, minute, intervals)
                    }
                    showSheet = false
                    topicToEdit = null
                    if (isFirstTopic) {
                        checkBatteryOptimizationPrompt()
                    }
                }
            )
        }

        if (showBatteryOptimizationDialog) {
            AlertDialog(
                onDismissRequest = { dismissBatteryOptimizationPrompt() },
                title = { Text(text = "Enable Timely Revision Alerts") },
                text = {
                    Text(
                        text = "To guarantee revision alarms ring on schedule when your phone is locked or idle, please exempt Study Plan from battery optimizations."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { openBatteryOptimizationSettings() }) {
                        Text(text = "Allow")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dismissBatteryOptimizationPrompt() }) {
                        Text(text = "Not Now")
                    }
                }
            )
        }
    }
}
