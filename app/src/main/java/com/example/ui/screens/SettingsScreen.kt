package com.example.ui.screens

import android.content.Intent
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
                            Triple("بعد العشاء", 21, 0)
                        )

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

        // Test Notification Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تجربة إشعار التذكير الآن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط على الزر أدناه لإرسال إشعار تجريبي في شريط الإشعارات العلوي للتأكد من وصول التنبيهات.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !QuranNotificationManager.canSendNotification(context)
                            ) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.sendTestNotification()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_test_notification_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إرسال إشعار تذكير تجريبي الآن",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. صفحة مدة استخدام الجهاز / وقت القراءة
// -------------------------------------------------------------
@Composable
fun ScreenTimeSettingsView(viewModel: QuranViewModel) {
    val settings by viewModel.settings.collectAsState()

    val goalMinutes = settings.dailyGoalMinutes
    val todayMinutes = settings.todayMinutesRead
    val progress = (todayMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Goal Ring Progress Hero
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
                    Text(
                        text = "إنجاز قراءة القرآن اليوم",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                        shape = RoundedCornerShape(8.dp),
                        color = if (percent >= 100) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (percent >= 100) "🎉 تم إكمال هدف اليوم بنجاح!" else "متبقي ${goalMinutes - todayMinutes} دقيقة لاكتمال ورد اليوم",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (percent >= 100) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Setting Daily Goal (تحديد وقت لقراءة القرآن)
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
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اختر الهدف اليومي المناسب لك للمداومة على تلاوة وتدبر كتاب الله",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overall Reading Analytics
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إحصائيات القراءة والتلاوة",
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
                                text = "أيام التتابع المتواصلة",
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
                                text = "إجمالي الوقت المقروء",
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
                        text = "الإصدار 1.0.0 • وقف لوجه الله تعالى",
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
