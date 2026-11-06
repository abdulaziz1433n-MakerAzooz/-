package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.model.AppSettings
import com.example.data.model.MushafItem
import com.example.data.model.UserProfile
import com.example.data.notification.QuranNotificationManager
import com.example.data.repository.FirestoreUserRepository
import com.example.data.repository.QuranRepository
import com.example.ui.theme.ReadingPaperMode
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    MUSHAFS("المصاحف"),
    HISTORY("سجل الملفات"),
    PROFILE("البروفايل"),
    SETTINGS("الإعدادات"),
    READER("القارئ")
}

enum class SettingsSubPage(val title: String, val index: Int) {
    APPEARANCE("المظهر", 0),
    NOTIFICATIONS("الإشعارات", 1),
    SCREEN_TIME("مدة استخدام الجهاز والرفاهية", 2),
    ABOUT("حول", 3)
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application)
    val authManager = AuthManager(application)
    val firestoreRepository = FirestoreUserRepository(application)

    val mushafs: StateFlow<List<MushafItem>> = repository.mushafs
    val settings: StateFlow<AppSettings> = repository.settings
    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser

    // Cloud User Profile from Google Sign-In
    val userProfile: StateFlow<UserProfile?> = currentUser.flatMapLatest { user ->
        if (user != null) {
            firestoreRepository.observeUserProfile(user.uid)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Email & Password with OTP verification state
    private val _emailAuthProfile = MutableStateFlow<UserProfile?>(loadSavedEmailProfile())
    val emailAuthProfile: StateFlow<UserProfile?> = _emailAuthProfile.asStateFlow()

    val effectiveProfile: StateFlow<UserProfile?> = combine(userProfile, _emailAuthProfile) { cloud, localEmail ->
        cloud ?: localEmail
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _emailAuthProfile.value)

    private val _isSignUpMode = MutableStateFlow(true)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _fullNameInput = MutableStateFlow("")
    val fullNameInput: StateFlow<String> = _fullNameInput.asStateFlow()

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _passwordInput = MutableStateFlow("")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _confirmPasswordInput = MutableStateFlow("")
    val confirmPasswordInput: StateFlow<String> = _confirmPasswordInput.asStateFlow()

    private val _selectedKhatmahGoal = MutableStateFlow("ختمة كل شهر (جزء يومياً)")
    val selectedKhatmahGoal: StateFlow<String> = _selectedKhatmahGoal.asStateFlow()

    private val _otpInput = MutableStateFlow("")
    val otpInput: StateFlow<String> = _otpInput.asStateFlow()

    private val _generatedOtp = MutableStateFlow<String?>(null)
    val generatedOtp: StateFlow<String?> = _generatedOtp.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

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

        // When Google user signs in, initialize or sync profile
        viewModelScope.launch {
            currentUser.collectLatest { user ->
                if (user != null) {
                    val initialProfile = UserProfile(
                        userId = user.uid,
                        displayName = user.displayName ?: "قارئ القرآن",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString() ?: "",
                        dailyReadingGoalMinutes = settings.value.dailyGoalMinutes.toLong(),
                        totalMinutesRead = settings.value.totalMinutesRead.toLong(),
                        totalPagesRead = settings.value.totalPagesRead.toLong(),
                        currentStreakDays = settings.value.streakDays.toLong()
                    )
                    firestoreRepository.saveUserProfile(initialProfile)
                }
            }
        }
    }

    private fun loadSavedEmailProfile(): UserProfile? {
        val prefs = getApplication<Application>().getSharedPreferences("email_auth_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("email", null) ?: return null
        val name = prefs.getString("name", "قارئ القرآن") ?: "قارئ القرآن"
        val goal = prefs.getString("goal", "ختمة كل شهر (جزء يومياً)") ?: "ختمة كل شهر (جزء يومياً)"
        return UserProfile(
            userId = "email_" + email.replace(Regex("[^a-zA-Z0-9]"), "_"),
            displayName = name,
            email = email,
            khatmahGoal = goal,
            dailyReadingGoalMinutes = prefs.getLong("goal_mins", 30L),
            totalMinutesRead = prefs.getLong("total_mins", 45L),
            totalPagesRead = prefs.getLong("total_pages", 22L),
            currentStreakDays = prefs.getLong("streak", 2L)
        )
    }

    private fun saveEmailProfile(profile: UserProfile?) {
        val prefs = getApplication<Application>().getSharedPreferences("email_auth_prefs", Context.MODE_PRIVATE)
        if (profile == null) {
            prefs.edit().clear().apply()
        } else {
            prefs.edit()
                .putString("email", profile.email)
                .putString("name", profile.displayName)
                .putString("goal", profile.khatmahGoal)
                .putLong("goal_mins", profile.dailyReadingGoalMinutes)
                .putLong("total_mins", profile.totalMinutesRead)
                .putLong("total_pages", profile.totalPagesRead)
                .putLong("streak", profile.currentStreakDays)
                .apply()
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectSettingsSubPage(page: SettingsSubPage) {
        _settingsSubPage.value = page
    }

    fun toggleAuthMode(signUp: Boolean) {
        _isSignUpMode.value = signUp
        resetEmailAuth()
    }

    fun updateFullNameInput(name: String) {
        _fullNameInput.value = name
    }

    fun updateEmailInput(email: String) {
        _emailInput.value = email
    }

    fun updatePasswordInput(password: String) {
        _passwordInput.value = password
    }

    fun updateConfirmPasswordInput(password: String) {
        _confirmPasswordInput.value = password
    }

    fun updateSelectedKhatmahGoal(goal: String) {
        _selectedKhatmahGoal.value = goal
    }

    fun updateOtpInput(otp: String) {
        _otpInput.value = otp
    }

    fun sendEmailOtp(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val email = _emailInput.value.trim()
        val password = _passwordInput.value.trim()
        val isSignUp = _isSignUpMode.value

        if (isSignUp && _fullNameInput.value.trim().isBlank()) {
            onError("يرجى كتابة الاسم الكريم لإنشاء الحساب")
            return
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("يرجى إدخال بريد إلكتروني صحيح")
            return
        }

        if (password.length < 6) {
            onError("كلمة المرور يجب أن لا تقل عن 6 أحرف أو أرقام")
            return
        }

        if (isSignUp && password != _confirmPasswordInput.value.trim()) {
            onError("كلمة المرور وتأكيد كلمة المرور غير متطابقتين")
            return
        }

        // Generate 6-digit OTP code
        val code = (100000..999999).random().toString()
        _generatedOtp.value = code
        _isOtpSent.value = true
        onSuccess(code)
    }

    fun verifyEmailOtp(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val entered = _otpInput.value.trim()
        val expected = _generatedOtp.value
        if (entered.isBlank() || entered != expected) {
            onError("رمز التحقق غير صحيح، يرجى التأكد من الرمز المكون من 6 أرقام")
            return
        }

        val email = _emailInput.value.trim()
        val name = if (_isSignUpMode.value && _fullNameInput.value.isNotBlank()) {
            _fullNameInput.value.trim()
        } else {
            email.substringBefore('@').replaceFirstChar { it.uppercase() }
        }

        val newProfile = UserProfile(
            userId = "email_" + email.replace(Regex("[^a-zA-Z0-9]"), "_"),
            displayName = name,
            email = email,
            khatmahGoal = _selectedKhatmahGoal.value,
            dailyReadingGoalMinutes = settings.value.dailyGoalMinutes.toLong(),
            totalMinutesRead = settings.value.totalMinutesRead.toLong(),
            totalPagesRead = settings.value.totalPagesRead.toLong(),
            currentStreakDays = settings.value.streakDays.toLong()
        )
        _emailAuthProfile.value = newProfile
        saveEmailProfile(newProfile)
        _isOtpSent.value = false
        _otpInput.value = ""
        _generatedOtp.value = null
        onSuccess()
    }

    fun resetEmailAuth() {
        _isOtpSent.value = false
        _otpInput.value = ""
        _generatedOtp.value = null
    }

    fun signOut(activity: Activity? = null) {
        authManager.signOut(viewModelScope) {
            _emailAuthProfile.value = null
            saveEmailProfile(null)
            _emailInput.value = ""
            _passwordInput.value = ""
            _otpInput.value = ""
            _fullNameInput.value = ""
            _confirmPasswordInput.value = ""
            _isOtpSent.value = false
            _generatedOtp.value = null
            Toast.makeText(getApplication(), "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateKhatmahGoal(newGoal: String) {
        val uid = currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository.updateKhatmahGoal(uid, newGoal)
                Toast.makeText(getApplication(), "تم تحديث هدف الختمة بنجاح", Toast.LENGTH_SHORT).show()
            }
        } else {
            val emailProf = _emailAuthProfile.value
            if (emailProf != null) {
                val updated = emailProf.copy(khatmahGoal = newGoal)
                _emailAuthProfile.value = updated
                saveEmailProfile(updated)
                Toast.makeText(getApplication(), "تم تحديث هدف الختمة بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startReading(mushaf: MushafItem, targetPage: Int? = null) {
        _currentReadingMushaf.value = mushaf
        _currentPage.value = targetPage ?: mushaf.lastReadPage.coerceAtLeast(1)
        _currentTab.value = AppTab.READER

        // Record a new reading session
        val currentS = settings.value
        repository.updateSettings(currentS.copy(todaySessionsCount = currentS.todaySessionsCount + 1))

        _sessionDurationSeconds.value = 0L
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var lastEyeBreakMinute = 0
            var goalAchievedFired = false
            var limitFired = false

            while (true) {
                delay(1000)
                _sessionDurationSeconds.value += 1
                val totalSeconds = _sessionDurationSeconds.value

                if (totalSeconds % 60 == 0L) {
                    val sessionMins = (totalSeconds / 60).toInt()
                    repository.logReadingMinutes(1)
                    currentUser.value?.uid?.let { uid ->
                        firestoreRepository.syncReadingProgress(
                            userId = uid,
                            page = _currentPage.value,
                            mushafTitle = mushaf.title,
                            mushafId = mushaf.id,
                            minutesSpent = 1
                        )
                    }

                    val s = settings.value
                    // Digital Wellbeing: Eye break alert (20-20-20 rule)
                    if (s.notificationsEnabled && s.eyeBreakReminderEnabled && s.eyeBreakIntervalMinutes > 0) {
                        if (sessionMins > 0 && sessionMins % s.eyeBreakIntervalMinutes == 0 && sessionMins != lastEyeBreakMinute) {
                            lastEyeBreakMinute = sessionMins
                            QuranNotificationManager.sendEyeBreakNotification(
                                getApplication(),
                                s.eyeBreakIntervalMinutes
                            )
                        }
                    }

                    // Digital Wellbeing: Daily goal achieved notification
                    if (s.notificationsEnabled && s.goalAchievedNotificationEnabled && !goalAchievedFired) {
                        if (s.todayMinutesRead >= s.dailyGoalMinutes) {
                            goalAchievedFired = true
                            QuranNotificationManager.sendGoalAchievedNotification(
                                getApplication(),
                                s.dailyGoalMinutes
                            )
                        }
                    }

                    // Digital Wellbeing: Reading session limit notification
                    if (s.notificationsEnabled && s.readingLimitEnabled && !limitFired) {
                        if (sessionMins >= s.readingLimitMinutes) {
                            limitFired = true
                            QuranNotificationManager.sendReadingLimitNotification(
                                getApplication(),
                                s.readingLimitMinutes
                            )
                        }
                    }
                }
            }
        }
    }

    fun closeReader() {
        val currentMushaf = _currentReadingMushaf.value
        val page = _currentPage.value
        if (currentMushaf != null) {
            repository.updateReadingProgress(currentMushaf.id, page)
            currentUser.value?.uid?.let { uid ->
                viewModelScope.launch {
                    val mins = (_sessionDurationSeconds.value / 60).toInt()
                    firestoreRepository.syncReadingProgress(
                        userId = uid,
                        page = page,
                        mushafTitle = currentMushaf.title,
                        mushafId = currentMushaf.id,
                        minutesSpent = mins
                    )
                }
            }
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

        currentUser.value?.uid?.let { uid ->
            viewModelScope.launch {
                firestoreRepository.syncReadingProgress(
                    userId = uid,
                    page = clamped,
                    mushafTitle = mushaf.title,
                    mushafId = mushaf.id
                )
            }
        }
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
        _currentReadingMushaf.value = mushafs.value.find { it.id == mushaf.id }
    }

    fun downloadMushaf(mushafId: String) {
        viewModelScope.launch {
            Toast.makeText(getApplication(), "بدأ تحميل المصحف الشريف...", Toast.LENGTH_SHORT).show()
            val result = repository.downloadMushaf(mushafId) { progress ->
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
        currentUser.value?.uid?.let { uid ->
            viewModelScope.launch {
                firestoreRepository.updateDailyReadingGoal(uid, minutes.toLong())
            }
        }
        Toast.makeText(getApplication(), "تم تحديد هدف التلاوة: $minutes دقيقة يومياً", Toast.LENGTH_SHORT).show()
    }

    // --- Digital Wellbeing and Notifications Controls ---

    fun toggleEyeBreakReminder(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(eyeBreakReminderEnabled = enabled))
        val msg = if (enabled) "تم تفعيل تنبيه استراحة العين (قاعدة 20-20-20)" else "تم إيقاف تنبيه استراحة العين"
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    fun updateEyeBreakInterval(minutes: Int) {
        repository.updateSettings(settings.value.copy(eyeBreakIntervalMinutes = minutes))
        Toast.makeText(getApplication(), "تم ضبط استراحة العين كل $minutes دقيقة", Toast.LENGTH_SHORT).show()
    }

    fun toggleBedtimeReminder(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(bedtimeReminderEnabled = enabled))
        val msg = if (enabled) "تم تفعيل تذكير سورة الملك ووقت النوم 🌙" else "تم إيقاف تذكير وقت النوم"
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    fun updateBedtime(hour: Int, minute: Int) {
        repository.updateSettings(settings.value.copy(bedtimeHour = hour, bedtimeMinute = minute))
        Toast.makeText(
            getApplication(),
            "تم ضبط وقت تذكير النوم على: %02d:%02d".format(hour, minute),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun toggleGoalAchievedNotification(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(goalAchievedNotificationEnabled = enabled))
        val msg = if (enabled) "تم تفعيل إشعار إنجاز الهدف اليومي 🎉" else "تم إيقاف إشعار إنجاز الهدف اليومي"
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    fun toggleReadingLimit(enabled: Boolean) {
        repository.updateSettings(settings.value.copy(readingLimitEnabled = enabled))
        val msg = if (enabled) "تم تفعيل تنبيه الحد الأقصى للجلسة ⏳" else "تم إيقاف تنبيه الحد الأقصى للجلسة"
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    fun updateReadingLimit(minutes: Int) {
        repository.updateSettings(settings.value.copy(readingLimitMinutes = minutes))
        Toast.makeText(getApplication(), "تم ضبط الحد الأقصى للجلسة: $minutes دقيقة", Toast.LENGTH_SHORT).show()
    }

    fun openSystemDigitalWellbeing(context: Context) {
        val opened = QuranNotificationManager.openSystemDigitalWellbeing(context)
        if (!opened) {
            Toast.makeText(getApplication(), "تعذر فتح إعدادات الرفاهية الرقمية، يرجى فتح إعدادات الهاتف", Toast.LENGTH_LONG).show()
        }
    }

    // --- Interactive Notification Dispatchers ---

    fun sendTestDailyReminder() {
        val sent = QuranNotificationManager.sendDailyReminderNotification(
            context = getApplication(),
            title = "حان موعد وردك اليومي 📖",
            message = "«اقْرَؤُوا القُرْآنَ فإنَّه يَأْتي يَومَ القِيامَةِ شَفِيعًا لأَصْحابِهِ» افتح مصحفك الآن وواصل التلاوة."
        )
        showNotificationToast(sent)
    }

    fun sendTestEyeBreakNotification() {
        val sent = QuranNotificationManager.sendEyeBreakNotification(
            context = getApplication(),
            readingMinutes = settings.value.eyeBreakIntervalMinutes
        )
        showNotificationToast(sent)
    }

    fun sendTestGoalAchievedNotification() {
        val sent = QuranNotificationManager.sendGoalAchievedNotification(
            context = getApplication(),
            goalMinutes = settings.value.dailyGoalMinutes
        )
        showNotificationToast(sent)
    }

    fun sendTestBedtimeNotification() {
        val sent = QuranNotificationManager.sendBedtimeReminderNotification(
            context = getApplication()
        )
        showNotificationToast(sent)
    }

    fun sendTestReadingLimitNotification() {
        val sent = QuranNotificationManager.sendReadingLimitNotification(
            context = getApplication(),
            limitMinutes = settings.value.readingLimitMinutes
        )
        showNotificationToast(sent)
    }

    fun sendTestNotification() {
        sendTestDailyReminder()
    }

    private fun showNotificationToast(sent: Boolean) {
        if (sent) {
            Toast.makeText(getApplication(), "تم إرسال الإشعار بنجاح إلى شريط الإشعارات ✅", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "يرجى السماح بإشعارات التطبيق في إعدادات النظام لتلقي التنبيهات", Toast.LENGTH_LONG).show()
        }
    }
}
