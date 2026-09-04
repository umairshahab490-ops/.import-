package com.umairshahab.etea.studyplan.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

object Motion {
    const val FAST = 200
    const val MEDIUM = 300
    val STANDARD: Easing = FastOutSlowInEasing
    val DECELERATE: Easing = LinearOutSlowInEasing
}
