package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.MushafItem
import com.example.data.notification.QuranNotificationManager
import com.example.data.repository.QuranRepository
import com.example.ui.theme.ReadingPaperMode
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    MUSHAFS("تحميل المصاحف"),
    HISTORY("سجل الملفات"),
    SETTINGS("الإعدادات"),
    READER("القارئ")
}

enum class SettingsSubPage(val title: String, val index: Int) {
    APPEARANCE("المظهر", 0),
    NOTIFICATIONS("الإشعارات", 1),
    SCREEN_TIME("مدة استخدام الجهاز", 2),
    ABOUT("حول", 3)
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application)

    val mushafs: StateFlow<List<MushafItem>> = repository.mushafs
    val settings: StateFlow<AppSettings> = repository.settings

    private val _currentTab = MutableStateFlow(AppTab.MUSHAFS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _settingsSubPage = MutableStateFlow(SettingsSubPage.APPEARANCE)
    val settingsSubPage: StateFlow<SettingsSubPage> = _settingsSubPage.asStateFlow()

    private val _currentReadingMushaf = MutableStateFlow<MushafItem?>(null)
    val currentReadingMushaf: StateFlow<MushafItem?> = _currentReadingMushaf.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _sessionDurationSeconds = MutableStateFlow(0L)
    val sessionDurationSeconds: StateFlow<Long> = _sessionDurationSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        QuranNotificationManager.createNotificationChannel(application)
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectSettingsSubPage(page: SettingsSubPage) {
        _settingsSubPage.value = page
    }

    fun startReading(mushaf: MushafItem, targetPage: Int? = null) {
        _currentReadingMushaf.value = mushaf
        _currentPage.value = targetPage ?: mushaf.lastReadPage.coerceAtLeast(1)
        _currentTab.value = AppTab.READER

        // Start reading timer
        _sessionDurationSeconds.value = 0L
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _sessionDurationSeconds.value += 1
                if (_sessionDurationSeconds.value % 60 == 0L) {
                    repository.logReadingMinutes(1)
                }
            }
        }
    }

    fun closeReader() {
        val currentMushaf = _currentReadingMushaf.value
        if (currentMushaf != null) {
            repository.updateReadingProgress(currentMushaf.id, _currentPage.value)
        }
        val mins = (_sessionDurationSeconds.value / 60).toInt()
        if (mins > 0) {
            repository.logReadingMinutes(mins)
        }
        timerJob?.cancel()
        timerJob = null
        _currentReadingMushaf.value = null
        _currentTab.value = AppTab.HISTORY
    }

    fun jumpToPage(page: Int) {
        val mushaf = _currentReadingMushaf.value ?: return
        val clamped = page.coerceIn(1, mushaf.totalPages)
        _currentPage.value = clamped
        repository.updateReadingProgress(mushaf.id, clamped)
    }

    fun nextPage() {
        val mushaf = _currentReadingMushaf.value ?: return
        if (_currentPage.value < mushaf.totalPages) {
            jumpToPage(_currentPage.value + 1)
        }
    }

    fun prevPage() {
        if (_currentPage.value > 1) {
            jumpToPage(_currentPage.value - 1)
        }
    }

    fun toggleBookmarkForCurrentPage() {
        val mushaf = _currentReadingMushaf.value ?: return
        repository.toggleBookmark(mushaf.id, _currentPage.value)
        // Refresh local instance
        _currentReadingMushaf.value = mushafs.value.find { it.id == mushaf.id }
    }

    fun downloadMushaf(mushafId: String) {
        viewModelScope.launch {
            Toast.makeText(getApplication(), "بدأ تحميل المصحف الشريف...", Toast.LENGTH_SHORT).show()
            val result = repository.downloadMushaf(mushafId) { progress ->
                // Progress handled by repository StateFlow
            }
            if (result.isSuccess) {
                Toast.makeText(getApplication(), "تم اكتمال تحميل المصحف بنجاح 📖", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "حدث خطأ أثناء التحميل: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importPdf(uri: Uri, name: String) {
        viewModelScope.launch {
            val result = repository.importPdfFromUri(uri, name)
            if (result.isSuccess) {
                Toast.makeText(getApplication(), "تم استيراد المصحف بنجاح 📖", Toast.LENGTH_SHORT).show()
                result.getOrNull()?.let { startReading(it) }
            } else {
                Toast.makeText(getApplication(), "فشل الاستيراد: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteMushaf(mushafId: String) {
        val success = repository.deleteMushafFile(mushafId)
        if (success) {
            Toast.makeText(getApplication(), "تم حذف الملف بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        repository.updateSettings(settings.value.copy(themeMode = mode))
    }

    fun updateThemePalette(palette: ThemePalette) {
        repository.updateSettings(settings.value.copy(themePalette = palette))
    }

    fun updateReadingPaperMode(paper: ReadingPaperMode) {
        repository.updateSettings(settings.value.copy(readingPaperMode = paper))
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(keepScreenOn = enabled))
    }

    fun toggleNotifications(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(notificationsEnabled = enabled))
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        repository.updateSettings(settings.value.copy(reminderHour = hour, reminderMinute = minute))
        Toast.makeText(
            getApplication(),
            "تم ضبط وقت التذكير على: %02d:%02d".format(hour, minute),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun updateDailyGoal(minutes: Int) {
        repository.updateSettings(settings.value.copy(dailyGoalMinutes = minutes))
        Toast.makeText(getApplication(), "تم تحديد هدف التلاوة: $minutes دقيقة يومياً", Toast.LENGTH_SHORT).show()
    }

    fun sendTestNotification() {
        val sent = QuranNotificationManager.sendTestNotification(
            context = getApplication(),
            title = "حان موعد وردك اليومي 📖",
            message = "قال رسول الله ﷺ: «خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ». افتح مصحفك واقرأ وردك الآن."
        )
        if (sent) {
            Toast.makeText(getApplication(), "تم إرسال إشعار تذكير تجريبي بنجاح ✅", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "يرجى السماح بإشعارات التطبيق في إعدادات النظام", Toast.LENGTH_LONG).show()
        }
    }
}
