package com.example.data.model

import com.example.ui.theme.ReadingPaperMode
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette

data class MushafItem(
    val id: String,
    val title: String,
    val riwayah: String,
    val description: String,
    val totalPages: Int,
    val fileSizeMb: Double,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val downloadUrl: String = "",
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    val lastReadPage: Int = 1,
    val lastReadTimestamp: Long = 0L,
    val bookmarks: List<Int> = emptyList(),
    val isStarter: Boolean = false,
    val isUserImported: Boolean = false
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themePalette: ThemePalette = ThemePalette.EMERALD,
    val readingPaperMode: ReadingPaperMode = ReadingPaperMode.NORMAL,
    val keepScreenOn: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 30,
    val reminderTitle: String = "وردك اليومي من القرآن الكريم",
    val dailyGoalMinutes: Int = 30,
    val todayMinutesRead: Int = 0,
    val streakDays: Int = 3,
    val totalMinutesRead: Int = 85,
    val totalPagesRead: Int = 42,
    val lastActiveDate: String = ""
)

data class QuranBookmark(
    val mushafId: String,
    val mushafTitle: String,
    val pageNumber: Int,
    val timestamp: Long,
    val note: String = ""
)
