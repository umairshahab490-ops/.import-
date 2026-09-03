package com.umairshahab.etea.studyplan

import android.app.Application
import com.umairshahab.etea.studyplan.data.local.AppDatabase

class StudyPlanApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
}
