package com.example.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MushafItem
import com.example.ui.theme.ReadingPaperMode
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranPdfReaderScreen(
    mushaf: MushafItem,
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPage by viewModel.currentPage.collectAsState()
    val sessionSeconds by viewModel.sessionDurationSeconds.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showPaperModeDialog by remember { mutableStateOf(false) }
    var showIndexDialog by remember { mutableStateOf(false) }

    // Zoom and pan state
    var scale by remember(currentPage) { mutableFloatStateOf(1f) }
    var offset by remember(currentPage) { mutableStateOf(Offset.Zero) }

    // Screen awake lock
    DisposableEffect(settings.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Intercept back button to save progress
    BackHandler {
        viewModel.closeReader()
    }

    // Persistent PdfRenderer instance for high-speed page turning
    var pdfRenderer by remember(mushaf.localPath) { mutableStateOf<PdfRenderer?>(null) }
    var pfd by remember(mushaf.localPath) { mutableStateOf<ParcelFileDescriptor?>(null) }
    var totalPagesCount by remember(mushaf.localPath) { mutableIntStateOf(mushaf.totalPages) }

    DisposableEffect(mushaf.localPath) {
        val path = mushaf.localPath
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                try {
                    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(descriptor)
                    pfd = descriptor
                    pdfRenderer = renderer
                    totalPagesCount = renderer.pageCount
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        onDispose {
            try {
                pdfRenderer?.close()
                pfd?.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Rendered Bitmap state
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }

    // Render current page when renderer or page changes
    LaunchedEffect(pdfRenderer, currentPage) {
        val renderer = pdfRenderer ?: return@LaunchedEffect
        isLoadingPage = true
        withContext(Dispatchers.IO) {
            try {
                val pageIdx = (currentPage - 1).coerceIn(0, renderer.pageCount - 1)
                val page = renderer.openPage(pageIdx)

                val renderWidth = page.width * 2
                val renderHeight = page.height * 2
                val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                withContext(Dispatchers.Main) {
                    pageBitmap = bitmap
                    isLoadingPage = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingPage = false
                }
            }
        }
    }

    val isBookmarked = mushaf.bookmarks.contains(currentPage)

    // Calculate background and paper colors
    val paperBgColor = when (settings.readingPaperMode) {
        ReadingPaperMode.NORMAL -> Color(0xFFF9F9F9)
        ReadingPaperMode.SEPIA -> Color(0xFFF6F0DF)
        ReadingPaperMode.NIGHT -> Color(0xFF121212)
    }

    val paperFrameColor = when (settings.readingPaperMode) {
        ReadingPaperMode.NORMAL -> Color(0xFFE2E8F0)
        ReadingPaperMode.SEPIA -> Color(0xFFD4AF37)
        ReadingPaperMode.NIGHT -> Color(0xFF2B2B2B)
    }

    // Color filter for Sepia and true Night mode (inversion)
    val pageColorFilter: ColorFilter? = remember(settings.readingPaperMode) {
        when (settings.readingPaperMode) {
            ReadingPaperMode.NORMAL -> null
            ReadingPaperMode.SEPIA -> {
                val sepiaMatrix = ColorMatrix(
                    floatArrayOf(
                        0.95f, 0.05f, 0.0f,  0f, 15f,
                        0.0f,  0.90f, 0.05f, 0f, 10f,
                        0.0f,  0.0f,  0.80f, 0f, 0f,
                        0f,    0f,    0f,    1f, 0f
                    )
                )
                ColorFilter.colorMatrix(sepiaMatrix)
            }
            ReadingPaperMode.NIGHT -> {
                // Invert colors so white page becomes pitch black and black text becomes white
                val invertMatrix = ColorMatrix(
                    floatArrayOf(
                        -1f,  0f,  0f, 0f, 255f,
                         0f, -1f,  0f, 0f, 255f,
                         0f,  0f, -1f, 0f, 255f,
                         0f,  0f,  0f, 1f,   0f
                    )
                )
                ColorFilter.colorMatrix(invertMatrix)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(paperBgColor)
    ) {
        // Main Quran Page with Gesture Detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentPage) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            val maxOffsetX = (size.width * (scale - 1)) / 2
                            val maxOffsetY = (size.height * (scale - 1)) / 2
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                }
                .pointerInput(currentPage) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1.2f) 1f else 2.2f
                            offset = Offset.Zero
                        },
                        onTap = { tapOffset ->
                            if (scale <= 1.1f) {
                                val screenWidth = size.width
                                when {
                                    // Right 25% tapped -> Previous page (RTL Quran navigation)
                                    tapOffset.x > screenWidth * 0.75f -> viewModel.prevPage()
                                    // Left 25% tapped -> Next page (RTL Quran navigation)
                                    tapOffset.x < screenWidth * 0.25f -> viewModel.nextPage()
                                    // Center tapped -> Toggle controls
                                    else -> showControls = !showControls
                                }
                            } else {
                                showControls = !showControls
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingPage) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري عرض الصفحة $currentPage...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (pageBitmap != null) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = paperBgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (showControls) 16.dp else 4.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .border(1.5.dp, paperFrameColor, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        bitmap = pageBitmap!!.asImageBitmap(),
                        contentDescription = "صفحة المصحف $currentPage",
                        contentScale = ContentScale.Fit,
                        colorFilter = pageColorFilter,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            } else {
                Text(
                    text = "تعذر فتح صفحة المصحف",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 15.sp
                )
            }
        }

        // Top Controls Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.closeReader() },
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mushaf.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "صفحة $currentPage من $totalPagesCount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Reading session timer pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        val mins = sessionSeconds / 60
                        val secs = sessionSeconds % 60
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%02d:%02d".format(mins, secs),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Paper Mode Dialog Button
                    IconButton(onClick = { showPaperModeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "نمط الورق",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Surah & Juz Index Button
                    IconButton(
                        onClick = { showIndexDialog = true },
                        modifier = Modifier.testTag("surah_index_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "فهرس السور والأجزاء",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Bookmark Button
                    IconButton(
                        onClick = { viewModel.toggleBookmarkForCurrentPage() },
                        modifier = Modifier.testTag("bookmark_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "علامة مرجعية",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Bottom Controls Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Page slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "١",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = currentPage.toFloat().coerceIn(1f, totalPagesCount.toFloat()),
                            onValueChange = { viewModel.jumpToPage(it.toInt()) },
                            valueRange = 1f..totalPagesCount.coerceAtLeast(1).toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "$totalPagesCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.prevPage() },
                            enabled = currentPage > 1,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NavigateBefore,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("السابقة")
                        }

                        // Jump to page button
                        OutlinedButton(
                            onClick = { showJumpDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صفحة $currentPage",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.nextPage() },
                            enabled = currentPage < totalPagesCount,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("التالية")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.NavigateNext,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }

        // Digital Wellbeing: Gentle In-Reader Eye Care Banner
        var dismissedBreakPeriod by remember { mutableLongStateOf(-1L) }
        val breakIntervalSec = (settings.eyeBreakIntervalMinutes * 60).toLong()
        val currentBreakPeriod = if (breakIntervalSec > 0) sessionSeconds / breakIntervalSec else 0L
        val showEyeBreakBanner = settings.eyeBreakReminderEnabled &&
                sessionSeconds >= breakIntervalSec &&
                breakIntervalSec > 0 &&
                (sessionSeconds % breakIntervalSec) < 30 &&
                dismissedBreakPeriod != currentBreakPeriod

        AnimatedVisibility(
            visible = showEyeBreakBanner,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "استراحة لصحة العينين 👀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "قاعدة 20-20-20: أغمض عينيك وانظر لبعيد لمدة 20 ثانية لحماية نظرك",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { dismissedBreakPeriod = currentBreakPeriod },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Jump to Page Dialog
    if (showJumpDialog) {
        var inputPage by remember { mutableStateOf(currentPage.toString()) }

        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = {
                Text(
                    text = "الانتقال إلى صفحة محددة",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "أدخل رقم الصفحة (من 1 إلى $totalPagesCount):",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputPage,
                        onValueChange = { inputPage = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = inputPage.toIntOrNull()
                        if (target != null) {
                            viewModel.jumpToPage(target.coerceIn(1, totalPagesCount))
                        }
                        showJumpDialog = false
                    }
                ) {
                    Text("انتقال")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Paper Tint Dialog
    if (showPaperModeDialog) {
        AlertDialog(
            onDismissRequest = { showPaperModeDialog = false },
            title = {
                Text(
                    text = "نمط صفحة القراءة",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReadingPaperMode.values().forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (settings.readingPaperMode == mode)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateReadingPaperMode(mode)
                                    showPaperModeDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.readingPaperMode == mode,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = mode.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = mode.desc,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaperModeDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Complete 114 Surahs & 30 Juzs Index Dialog
    if (showIndexDialog) {
        SurahJuzIndexDialog(
            maxPage = totalPagesCount,
            onSelectPage = { page ->
                viewModel.jumpToPage(page)
                showIndexDialog = false
            },
            onDismiss = { showIndexDialog = false }
        )
    }
}

@Composable
fun SurahJuzIndexDialog(
    maxPage: Int,
    onSelectPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "فهرس القرآن الكريم الشريف",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("السور (١١٤)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("الأجزاء (٣٠)") }
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 420.dp)) {
                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث عن سورة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    val filteredSurahs = remember(searchQuery) {
                        if (searchQuery.isBlank()) {
                            com.example.data.model.QuranIndexData.surahs
                        } else {
                            com.example.data.model.QuranIndexData.surahs.filter {
                                it.name.contains(searchQuery.trim())
                            }
                        }
                    }

                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredSurahs.size) { idx ->
                            val surah = filteredSurahs[idx]
                            val clampedPage = surah.startPage.coerceIn(1, maxPage)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPage(clampedPage) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${surah.number}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "سورة ${surah.name}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${surah.type} • ${surah.versesCount} آية",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "ص ${surah.startPage}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(com.example.data.model.QuranIndexData.juzs.size) { idx ->
                            val juz = com.example.data.model.QuranIndexData.juzs[idx]
                            val clampedPage = juz.startPage.coerceIn(1, maxPage)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPage(clampedPage) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = juz.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "ص ${juz.startPage}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}
