package com.example.ui.screens

import android.content.Intent
import org.json.JSONObject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.rememberCoroutineScope
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.notification.QuranNotificationManager
import com.example.ui.theme.ReadingPaperMode
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.SettingsSubPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val currentSubPage by viewModel.settingsSubPage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الإعدادات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal 4-Tab Navigation Bar
            ScrollableTabRow(
                selectedTabIndex = currentSubPage.index,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                SettingsSubPage.values().forEach { subPage ->
                    Tab(
                        selected = currentSubPage == subPage,
                        onClick = { viewModel.selectSettingsSubPage(subPage) },
                        modifier = Modifier.testTag("settings_tab_${subPage.name.lowercase()}"),
                        text = {
                            Text(
                                text = subPage.title,
                                fontWeight = if (currentSubPage == subPage) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (subPage) {
                                    SettingsSubPage.APPEARANCE -> Icons.Default.Palette
                                    SettingsSubPage.NOTIFICATIONS -> Icons.Default.Notifications
                                    SettingsSubPage.SCREEN_TIME -> Icons.Default.HourglassTop
                                    SettingsSubPage.ABOUT -> Icons.Default.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            // Sub-page content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentSubPage) {
                    SettingsSubPage.APPEARANCE -> AppearanceSettingsView(viewModel)
                    SettingsSubPage.NOTIFICATIONS -> NotificationSettingsView(viewModel)
                    SettingsSubPage.SCREEN_TIME -> ScreenTimeSettingsView(viewModel)
                    SettingsSubPage.ABOUT -> AboutSettingsView()
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. صفحة المظهر
// -------------------------------------------------------------
@Composable
fun AppearanceSettingsView(viewModel: QuranViewModel) {
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Mode Section (فاتح، داكن، تلقائي)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الوضع العام (داكن / فاتح / تلقائي)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeMode.values().forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (settings.themeMode == mode)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateThemeMode(mode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.themeMode == mode,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = when (mode) {
                                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                        ThemeMode.LIGHT -> Icons.Default.LightMode
                                        ThemeMode.DARK -> Icons.Default.DarkMode
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = mode.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Theme Palettes Section (السمات)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "سمات وألوان التطبيق",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اختر التنسيق اللوني المفضل للمصحف والقوائم",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ThemePalette.values().forEach { palette ->
                        val isSelected = settings.themePalette == palette
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(1.5.dp, palette.previewColor) else null,
                            color = if (isSelected) palette.previewColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.updateThemePalette(palette) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(palette.previewColor)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = palette.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = palette.previewColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reading Paper Tint Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خلفية ورق القراءة في المصحف",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ReadingPaperMode.values().forEach { paper ->
                        val isSelected = settings.readingPaperMode == paper
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateReadingPaperMode(paper) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = paper.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = paper.desc,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Screen Awake switch
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "إبقاء الشاشة مضاءة أثناء القراءة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "منع إيقاف تشغيل الشاشة التلقائي خلال تلاوة القرآن",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = settings.keepScreenOn,
                        onCheckedChange = { viewModel.toggleKeepScreenOn(it) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. صفحة الإشعارات
// -------------------------------------------------------------
@Composable
fun NotificationSettingsView(viewModel: QuranViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendTestNotification()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Notification Toggle
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (settings.notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "إشعارات التطبيق اليومية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (settings.notificationsEnabled) "الإشعارات مفعلة لتذكيرك بالورد القرآني" else "الإشعارات معطلة حالياً",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) },
                        modifier = Modifier.testTag("notifications_master_switch")
                    )
                }
            }
        }

        // Daily Reminder Time Picker
        item {
            AnimatedVisibility(visible = settings.notificationsEnabled) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "وقت التذكير اليومي بالورد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "حدد التوقيت الأنسب لك لتلقي رسالة التذكير بقراءة القرآن",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Current selected time display
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "%02d:%02d".format(settings.reminderHour, settings.reminderMinute),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (settings.reminderHour < 12) "صباحاً" else "مساءً",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "مواعيد سريعة مقترحة:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val presets = listOf(
                            Triple("بعد الفجر", 5, 30),
                            Triple("وقت الضحى", 9, 0),
                            Triple("بعد العصر", 16, 30),
                            Triple("بعد العشاء", 21, 0)                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { (label, h, m) ->
                                val isSelected = settings.reminderHour == h && settings.reminderMinute == m
                                OutlinedButton(
                                    onClick = { viewModel.updateReminderTime(h, m) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ) else ButtonDefaults.outlinedButtonColors(),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Test Notifications Hub Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تجربة واختبار الإشعارات فوراً",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط على أي زر لإرسال الإشعار فوراً إلى شريط إشعارات الهاتف والتأكد من وصوله:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    fun sendWithPerm(action: () -> Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !QuranNotificationManager.canSendNotification(context)
                        ) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            action()
                        }
                    }

                    // 1. Daily Quran Reminder
                    Button(
                        onClick = { sendWithPerm { viewModel.sendTestDailyReminder() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_test_notification_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. تجربة إشعار تذكير الورد اليومي 📖",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Eye Care (20-20-20)
                    OutlinedButton(
                        onClick = { sendWithPerm { viewModel.sendTestEyeBreakNotification() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. تجربة إشعار استراحة العين (20-20-20) 👀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Goal Achieved
                    OutlinedButton(
                        onClick = { sendWithPerm { viewModel.sendTestGoalAchievedNotification() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AlarmOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. تجربة إشعار إنجاز الهدف اليومي 🎉",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Bedtime / Surah Al-Mulk
                    OutlinedButton(
                        onClick = { sendWithPerm { viewModel.sendTestBedtimeNotification() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4. تجربة إشعار سورة الملك ووقت النوم 🌙",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. صفحة مدة استخدام الجهاز والرفاهية الرقمية
// -------------------------------------------------------------
@Composable
fun ScreenTimeSettingsView(viewModel: QuranViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission callback */ }

    fun checkAndSend(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !QuranNotificationManager.canSendNotification(context)
        ) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    val goalMinutes = settings.dailyGoalMinutes
    val todayMinutes = settings.todayMinutesRead
    val progress = (todayMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Goal Ring Progress Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الرفاهية الرقمية ووقت استخدام المصحف",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(130.dp),
                            strokeWidth = 10.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$todayMinutes",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "من $goalMinutes دقيقة",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (percent >= 100) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (percent >= 100) "🎉 تم إكمال هدف اليوم بنجاح!" else "متبقي ${goalMinutes - todayMinutes} دقيقة لاكتمال ورد اليوم",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (percent >= 100) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${settings.streakDays} أيام تتابع",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${settings.todaySessionsCount} جلسات اليوم",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 1. Android Digital Wellbeing Integration Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الربط مع الرفاهية الرقمية في الهاتف",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Digital Wellbeing & Screen Time",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "يمكنك إدارة وقت شاشة المصحف وتحديد مؤقت يومي للتطبيق وضبط وضع وقت النوم مباشرة من خلال نظام أندرويد لتعزيز التوازن الرقمي وصحتك.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.openSystemDigitalWellbeing(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_system_digital_wellbeing_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فتح إعدادات الرفاهية الرقمية في الهاتف 📲",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 2. Eye Care & 20-20-20 Rule Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "صحة العينين (قاعدة 20-20-20)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "تنبيه لأخذ استراحة كل ${settings.eyeBreakIntervalMinutes} دقيقة قراءة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = settings.eyeBreakReminderEnabled,
                            onCheckedChange = { viewModel.toggleEyeBreakReminder(it) },
                            modifier = Modifier.testTag("eye_break_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لحماية عينيك من الإجهاد والجفاف أثناء القراءة، يرسل التطبيق إشعاراً لطيفاً لتنظر لشيء بعيد (6 أمتار) لمدة 20 ثانية.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 16.sp
                    )

                    AnimatedVisibility(visible = settings.eyeBreakReminderEnabled) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "تكرار التنبيه أثناء القراءة:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val intervals = listOf(
                                Pair(15, "15 د"),
                                Pair(20, "20 د (الموصى بها)"),
                                Pair(30, "30 د"),
                                Pair(45, "45 د")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                intervals.forEach { (mins, label) ->
                                    val isSelected = settings.eyeBreakIntervalMinutes == mins
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateEyeBreakInterval(mins) },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { checkAndSend { viewModel.sendTestEyeBreakNotification() } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تجربة إشعار استراحة العين 👀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Bedtime & Wind-Down Reminder Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE7F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = Color(0xFF512DA8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تذكير سورة الملك ووقت النوم 🌙",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "تنبيه هادئ في الساعة %02d:%02d مساءً".format(settings.bedtimeHour, settings.bedtimeMinute),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = settings.bedtimeReminderEnabled,
                            onCheckedChange = { viewModel.toggleBedtimeReminder(it) },                            modifier = Modifier.testTag("bedtime_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "إشعار مسائي يذكرك بإنهاء يومك في سكينة مع سورة الملك المنجية من عذاب القبر وأذكار النوم المأثورة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 16.sp
                    )

                    AnimatedVisibility(visible = settings.bedtimeReminderEnabled) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "اختر موعد النوم المفضل:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val bedtimes = listOf(
                                Triple("9:30 م", 21, 30),
                                Triple("10:00 م", 22, 0),
                                Triple("10:30 م", 22, 30),
                                Triple("11:00 م", 23, 0)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                bedtimes.forEach { (label, h, m) ->
                                    val isSelected = settings.bedtimeHour == h && settings.bedtimeMinute == m
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateBedtime(h, m) },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { checkAndSend { viewModel.sendTestBedtimeNotification() } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تجربة إشعار وقت النوم 🌙", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Daily Goal & Achievement Notification Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AlarmOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحديد وقت لقراءة القرآن الكريم",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اختر الهدف اليومي المناسب لك للمداومة على تلاوة وتدبر كتاب الله",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val goals = listOf(
                        Pair(15, "15 دقيقة (ربع ساعة)"),
                        Pair(30, "30 دقيقة (نصف ساعة)"),
                        Pair(45, "45 دقيقة"),
                        Pair(60, "60 دقيقة (ساعة كاملة)")
                    )

                    goals.forEach { (mins, label) ->
                        val isSelected = settings.dailyGoalMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { viewModel.updateDailyGoal(mins) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إشعار عند إتمام الهدف اليومي 🎉",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "تلقي تهنئة تحفيزية فور إنجاز دقائق الورد اليومية",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = settings.goalAchievedNotificationEnabled,
                            onCheckedChange = { viewModel.toggleGoalAchievedNotification(it) },
                            modifier = Modifier.testTag("goal_achieved_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { checkAndSend { viewModel.sendTestGoalAchievedNotification() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تجربة إشعار إنجاز الهدف اليومي 🎉", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Reading Session Limit & Balance Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيه الحد الأقصى للجلسة المتواصلة ⏳",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (settings.readingLimitEnabled) "مفعل عند ${settings.readingLimitMinutes} دقيقة" else "معطل حالياً",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = settings.readingLimitEnabled,
                            onCheckedChange = { viewModel.toggleReadingLimit(it) },
                            modifier = Modifier.testTag("reading_limit_switch")
                        )
                    }

                    AnimatedVisibility(visible = settings.readingLimitEnabled) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            val limits = listOf(
                                Pair(45, "45 دقيقة"),
                                Pair(60, "60 دقيقة"),
                                Pair(90, "90 دقيقة")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                limits.forEach { (mins, label) ->
                                    val isSelected = settings.readingLimitMinutes == mins
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateReadingLimit(mins) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { checkAndSend { viewModel.sendTestReadingLimitNotification() } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تجربة إشعار الحد الأقصى ⏳", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 6. Overall Reading Analytics Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إحصائيات التلاوة الشاملة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "أيام التتابع",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${settings.streakDays} أيام 🔥",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Column {
                            Text(
                                text = "إجمالي الوقت",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${settings.totalMinutesRead} دقيقة ⏱️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Column {
                            Text(
                                text = "إجمالي الصفحات",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${settings.totalPagesRead} صفحة 📖",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. صفحة حول التطبيق
// -------------------------------------------------------------
@Composable
fun AboutSettingsView() {
    val context = LocalContext.current
    val currentVersion = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1" }
        catch (_: Exception) { "1.1" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "القرآن الكريم PDF",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "الإصدار $currentVersion • وقف لوجه الله تعالى",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "«إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ وَيُبَشِّرُ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا كَبِيرًا»",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }


        // App update checker (GitHub Releases)
        item {
            var updateMessage by remember { mutableStateOf("تحقق من توفر إصدار أحدث من التطبيق") }
            var latestReleaseUrl by remember { mutableStateOf<String?>(null) }
            var apkDownloadUrl by remember { mutableStateOf<String?>(null) }
            var checking by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val currentVersion = remember {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1"
                } catch (_: Exception) { "1.1" }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحديث التطبيق", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الإصدار المثبت: $currentVersion", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(updateMessage, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            checking = true
                            updateMessage = "جارٍ التحقق من GitHub..."
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    runCatching {
                                        val connection = (java.net.URL("https://api.github.com/repos/abdulaziz1433n-MakerAzooz/-/releases/latest").openConnection() as java.net.HttpURLConnection)
                                        connection.connectTimeout = 10000
                                        connection.readTimeout = 10000
                                        connection.setRequestProperty("Accept", "application/vnd.github+json")
                                        try {
                                            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                                            val tag = json.optString("tag_name").removePrefix("v")
                                            val page = json.optString("html_url")
                                            val assets = json.optJSONArray("assets")
                                            var apk: String? = null
                                            if (assets != null) {
                                                for (i in 0 until assets.length()) {
                                                    val asset = assets.getJSONObject(i)
                                                    if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                                                        apk = asset.optString("browser_download_url")
                                                        break
                                                    }
                                                }
                                            }
                                            Triple(tag, page, apk)
                                        } finally {
                                            connection.disconnect()
                                        }
                                    }
                                }
                                checking = false
                                result.onSuccess { (tag, page, apk) ->
                                    latestReleaseUrl = page
                                    apkDownloadUrl = apk
                                    val latestParts = tag.split(".").mapNotNull { it.toIntOrNull() }
                                    val currentParts = currentVersion.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
                                    val newer = (0 until maxOf(latestParts.size, currentParts.size)).firstNotNullOfOrNull { i ->
                                        val a = latestParts.getOrElse(i) { 0 }
                                        val b = currentParts.getOrElse(i) { 0 }
                                        if (a != b) a > b else null
                                    } ?: false
                                    updateMessage = when {
                                        tag.isBlank() -> "تعذر قراءة رقم الإصدار من GitHub."
                                        newer && apk != null -> "يتوفر إصدار جديد: $tag"
                                        newer -> "يتوفر إصدار $tag، لكن لم يُرفق ملف APK في صفحة الإصدار."
                                        else -> "تطبيقك محدث. أحدث إصدار منشور: $tag"
                                    }
                                }.onFailure {
                                    updateMessage = "تعذر الاتصال بـ GitHub. تحقق من الإنترنت وحاول مجدداً."
                                }
                            }
                        },
                        enabled = !checking,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (checking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (checking) "جارٍ التحقق..." else "التحقق من وجود تحديث")
                    }
                    if (apkDownloadUrl != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(apkDownloadUrl)))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تنزيل ملف التحديث APK")
                        }
                    } else if (latestReleaseUrl != null) {
                        TextButton(onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(latestReleaseUrl))) }
                        }) { Text("فتح صفحة الإصدار على GitHub") }
                    }
                }
            }
        }

        // Features & Editions info
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مميزات تطبيق القرآن PDF:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val features = listOf(
                        "تحميل مصاحف PDF متعددة الروايات (حفص، ورش، قالون، والتجويد الملون).",
                        "إمكانية استيراد أي ملف أو مصحف PDF من ذاكرة جهازك.",
                        "قارئ PDF مدمج فائق الوضوح يدعم التكبير، الإشارات المرجعية، ونمط سيبيا والوضع الليلي.",
                        "إشعارات وتذكيرات يومية لقراءة الورد مع إمكانية التحديد والتحكم الكامل.",
                        "تتبع مدة استخدام وقراءة القرآن وتحديد هدف يومي بالدقائق.",
                        "تخصيص المظهر بـ 5 سمات ألوان إسلامية راقية ودعم الوضع الداكن والفاتح."
                    )

                    features.forEach { feat ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feat,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Share & Support buttons
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "تطبيق القرآن الكريم PDF")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "أنصحك بتحميل واستخدام تطبيق القرآن الكريم PDF - لقراءة وتلاوة المصحف الشريف ومتابعة الورد اليومي وإشعارات التذكير."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاركة التطبيق ولك الأجر إن شاء الله")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "اللهم اجعل هذا العمل خالصاً لوجهك الكريم، وتقبله منا ومن كل من قرأ به أو نشره.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}