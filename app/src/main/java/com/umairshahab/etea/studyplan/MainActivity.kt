package com.umairshahab.etea.studyplan

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
}
