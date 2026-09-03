package com.umairshahab.etea.studyplan.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val BluePrimary = Color(0xFF3B82F6)
val PurpleSecondary = Color(0xFF8B5CF6)
val CanvasDark = Color(0xFF020617)
val CanvasLight = Color(0xFFF8FAFC)

val PrimaryGradientBrush = Brush.horizontalGradient(
    colors = listOf(BluePrimary, PurpleSecondary)
)

data class GlassmorphismColors(
    val isDark: Boolean,
    val canvas: Color,
    val backgroundGradient: Brush,
    val cardSurface: Color,
    val cardBorder: BorderStroke,
    val cardShape: RoundedCornerShape = RoundedCornerShape(20.dp),
    val metricTopicsBg: Color,
    val metricTopicsText: Color,
    val metricTodayBg: Color,
    val metricTodayText: Color,
    val metricMissedBg: Color,
    val metricMissedText: Color
)

val LocalGlassColors = staticCompositionLocalOf<GlassmorphismColors> {
    error("No GlassmorphismColors provided")
}

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    background = CanvasLight,
    onBackground = Color(0xFF0F172A),
    surface = Color.White.copy(alpha = 0.65f),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0).copy(alpha = 0.65f),
    onSurfaceVariant = Color(0xFF475569)
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    background = CanvasDark,
    onBackground = Color(0xFFF8FAFC),
    surface = Color.White.copy(alpha = 0.08f),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color.White.copy(alpha = 0.05f),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun StudyPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val glassColors = if (darkTheme) {
        GlassmorphismColors(
            isDark = true,
            canvas = CanvasDark,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    CanvasDark,
                    BluePrimary.copy(alpha = 0.12f),
                    PurpleSecondary.copy(alpha = 0.12f),
                    CanvasDark
                )
            ),
            cardSurface = Color.White.copy(alpha = 0.08f),
            cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            metricTopicsBg = Color(0xFF1E3A8A).copy(alpha = 0.40f),
            metricTopicsText = Color(0xFF93C5FD),
            metricTodayBg = Color(0xFF064E3B).copy(alpha = 0.40f),
            metricTodayText = Color(0xFF6EE7B7),
            metricMissedBg = Color(0xFF7F1D1D).copy(alpha = 0.40f),
            metricMissedText = Color(0xFFFCA5A5)
        )
    } else {
        GlassmorphismColors(
            isDark = false,
            canvas = CanvasLight,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    CanvasLight,
                    BluePrimary.copy(alpha = 0.06f),
                    PurpleSecondary.copy(alpha = 0.06f),
                    CanvasLight
                )
            ),
            cardSurface = Color.White.copy(alpha = 0.65f),
            cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
            metricTopicsBg = Color(0xFFEFF6FF).copy(alpha = 0.85f),
            metricTopicsText = Color(0xFF1D4ED8),
            metricTodayBg = Color(0xFFF0FDF4).copy(alpha = 0.85f),
            metricTodayText = Color(0xFF15803D),
            metricMissedBg = Color(0xFFFEF2F2).copy(alpha = 0.85f),
            metricMissedText = Color(0xFFB91C1C)
        )
    }

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

object StudyPlanThemeDefaults {
    val glassColors: GlassmorphismColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGlassColors.current
}
