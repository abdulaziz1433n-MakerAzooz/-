package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AuthGatewayScreen
import com.example.ui.screens.HistoryLibraryScreen
import com.example.ui.screens.MushafCatalogScreen
import com.example.ui.screens.QuranPdfReaderScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.QuranPdfTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.QuranViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val readingMushaf by viewModel.currentReadingMushaf.collectAsState()
            val effectiveProfile by viewModel.effectiveProfile.collectAsState()

            // Dynamic Custom Theme and RTL Arabic Layout Direction
            QuranPdfTheme(
                themeMode = settings.themeMode,
                themePalette = settings.themePalette
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (effectiveProfile == null) {
                            // Mandatory Authentication Gateway (Login & Sign Up required before using the app)
                            AuthGatewayScreen(viewModel = viewModel)
                        } else if (currentTab == AppTab.READER && readingMushaf != null) {
                            // Fullscreen Immersive Quran Reader
                            QuranPdfReaderScreen(
                                mushaf = readingMushaf!!,
                                viewModel = viewModel
                            )
                        } else {
                            // Main Application Scaffold with Bottom Navigation Bar
                            Scaffold(
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 6.dp,
                                        windowInsets = WindowInsets.navigationBars
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTab == AppTab.MUSHAFS,
                                            onClick = { viewModel.selectTab(AppTab.MUSHAFS) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == AppTab.MUSHAFS) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                                    contentDescription = "تحميل المصاحف"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = "المصاحف والتحميل",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (currentTab == AppTab.MUSHAFS) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_mushafs_tab")
                                        )

                                        NavigationBarItem(
                                            selected = currentTab == AppTab.HISTORY,
                                            onClick = { viewModel.selectTab(AppTab.HISTORY) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == AppTab.HISTORY) Icons.Filled.FolderSpecial else Icons.Outlined.FolderSpecial,
                                                    contentDescription = "سجل الملفات"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = "سجل الملفات",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (currentTab == AppTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_history_tab")
                                        )

                                        NavigationBarItem(
                                            selected = currentTab == AppTab.PROFILE,
                                            onClick = { viewModel.selectTab(AppTab.PROFILE) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == AppTab.PROFILE) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                                    contentDescription = "البروفايل"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = "البروفايل",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (currentTab == AppTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_profile_tab")
                                        )

                                        NavigationBarItem(
                                            selected = currentTab == AppTab.SETTINGS,
                                            onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                                    contentDescription = "الإعدادات"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = "الإعدادات",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag("nav_settings_tab")
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                AnimatedContent(
                                    targetState = currentTab,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "tab_transition",
                                    modifier = Modifier.padding(innerPadding)
                                ) { tab ->
                                    when (tab) {
                                        AppTab.MUSHAFS -> MushafCatalogScreen(viewModel = viewModel)
                                        AppTab.HISTORY -> HistoryLibraryScreen(viewModel = viewModel)
                                        AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                                        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                        AppTab.READER -> {
                                            if (readingMushaf != null) {
                                                QuranPdfReaderScreen(mushaf = readingMushaf!!, viewModel = viewModel)
                                            } else {
                                                MushafCatalogScreen(viewModel = viewModel)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
