package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val title: String) {
    SYSTEM("تلقائي (حسب النظام)"),
    LIGHT("الوضع الفاتح"),
    DARK("الوضع الداكن")
}

enum class ThemePalette(val title: String, val previewColor: Color) {
    EMERALD("الأخضر القرآني", EmeraldPrimary),
    ROYAL_NAVY("الأزرق الملكي", NavyPrimary),
    CLASSIC_GOLD("الذهبي الفاخر", GoldPrimary),
    LAVENDER("الخزامى الهادئ", LavenderPrimary),
    MAROON("العنابي الأندلسي", MaroonPrimary)
}

enum class ReadingPaperMode(val title: String, val desc: String) {
    NORMAL("الوضع العادي", "ورق ناصع البياض عالي الوضوح"),
    SEPIA("ورق مصحف قديم", "درجة سيبيا دافئة ومريحة للعينين"),
    NIGHT("الوضع الليلي المريح", "خلفية داكنة مع انعكاس ضوئي مريح")
}

fun getAppColorScheme(palette: ThemePalette, isDark: Boolean): ColorScheme {
    return when (palette) {
        ThemePalette.EMERALD -> if (isDark) {
            darkColorScheme(
                primary = EmeraldDarkPrimary,
                onPrimary = Color.Black,
                primaryContainer = EmeraldPrimary,
                onPrimaryContainer = Color.White,
                secondary = EmeraldSecondary,
                onSecondary = Color.Black,
                background = EmeraldDarkBackground,
                onBackground = Color(0xFFF0F0F0),
                surface = EmeraldDarkSurface,
                onSurface = Color(0xFFF0F0F0)
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimary,
                onPrimary = EmeraldOnPrimary,
                primaryContainer = EmeraldPrimaryContainer,
                onPrimaryContainer = EmeraldOnPrimaryContainer,
                secondary = EmeraldSecondary,
                onSecondary = Color.Black,
                background = EmeraldBackground,
                onBackground = Color(0xFF1A202C),
                surface = EmeraldSurface,
                onSurface = Color(0xFF1A202C)
            )
        }

        ThemePalette.ROYAL_NAVY -> if (isDark) {
            darkColorScheme(
                primary = NavyDarkPrimary,
                onPrimary = Color.Black,
                primaryContainer = NavyPrimary,
                onPrimaryContainer = Color.White,
                secondary = NavySecondary,
                onSecondary = Color.Black,
                background = NavyDarkBackground,
                onBackground = Color(0xFFF0F0F0),
                surface = NavyDarkSurface,
                onSurface = Color(0xFFF0F0F0)
            )
        } else {
            lightColorScheme(
                primary = NavyPrimary,
                onPrimary = NavyOnPrimary,
                primaryContainer = NavyPrimaryContainer,
                secondary = NavySecondary,
                background = NavyBackground,
                surface = NavySurface
            )
        }

        ThemePalette.CLASSIC_GOLD -> if (isDark) {
            darkColorScheme(
                primary = GoldDarkPrimary,
                onPrimary = Color.Black,
                primaryContainer = GoldPrimary,
                secondary = GoldDarkPrimary,
                background = GoldDarkBackground,
                surface = GoldDarkSurface
            )
        } else {
            lightColorScheme(
                primary = GoldPrimary,
                onPrimary = GoldOnPrimary,
                primaryContainer = GoldPrimaryContainer,
                secondary = GoldSecondary,
                background = GoldBackground,
                surface = GoldSurface
            )
        }

        ThemePalette.LAVENDER -> if (isDark) {
            darkColorScheme(
                primary = LavenderDarkPrimary,
                onPrimary = Color.Black,
                primaryContainer = LavenderPrimary,
                secondary = LavenderSecondary,
                background = LavenderDarkBackground,
                surface = LavenderDarkSurface
            )
        } else {
            lightColorScheme(
                primary = LavenderPrimary,
                onPrimary = LavenderOnPrimary,
                primaryContainer = LavenderPrimaryContainer,
                secondary = LavenderSecondary,
                background = LavenderBackground,
                surface = LavenderSurface
            )
        }

        ThemePalette.MAROON -> if (isDark) {
            darkColorScheme(
                primary = MaroonDarkPrimary,
                onPrimary = Color.Black,
                primaryContainer = MaroonPrimary,
                secondary = MaroonSecondary,
                background = MaroonDarkBackground,
                surface = MaroonDarkSurface
            )
        } else {
            lightColorScheme(
                primary = MaroonPrimary,
                onPrimary = MaroonOnPrimary,
                primaryContainer = MaroonPrimaryContainer,
                secondary = MaroonSecondary,
                background = MaroonBackground,
                surface = MaroonSurface
            )
        }
    }
}

@Composable
fun QuranPdfTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themePalette: ThemePalette = ThemePalette.EMERALD,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = getAppColorScheme(themePalette, isDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
