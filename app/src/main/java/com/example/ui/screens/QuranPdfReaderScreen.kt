package com.example.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

    // Zoom and pan state
    var scale by remember(currentPage) { mutableFloatStateOf(1f) }
    var offset by remember(currentPage) { mutableStateOf(Offset.Zero) }

    // Keep screen on effect
    DisposableEffect(settings.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Intercept hardware/system back button
    BackHandler {
        viewModel.closeReader()
    }

    // Rendered Bitmap state
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }

    // Render current PDF page
    LaunchedEffect(mushaf.localPath, currentPage) {
        isLoadingPage = true
        withContext(Dispatchers.IO) {
            val path = mushaf.localPath
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    try {
                        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = PdfRenderer(pfd)
                        val pageIdx = (currentPage - 1).coerceIn(0, renderer.pageCount - 1)
                        val page = renderer.openPage(pageIdx)

                        // Render at crisp 2x resolution
                        val renderWidth = page.width * 2
                        val renderHeight = page.height * 2
                        val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)

                        // Clear to white or paper color
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        page.close()
                        renderer.close()
                        pfd.close()

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
        }
    }

    val isBookmarked = mushaf.bookmarks.contains(currentPage)

    // Calculate background and tint colors based on ReadingPaperMode
    val paperBgColor = when (settings.readingPaperMode) {
        ReadingPaperMode.NORMAL -> Color(0xFFF9F9F9)
        ReadingPaperMode.SEPIA -> Color(0xFFF6F0DF)
        ReadingPaperMode.NIGHT -> Color(0xFF121212)
    }

    val paperFrameColor = when (settings.readingPaperMode) {
        ReadingPaperMode.NORMAL -> Color(0xFFE2E8F0)
        ReadingPaperMode.SEPIA -> Color(0xFFD4AF37)
        ReadingPaperMode.NIGHT -> Color(0xFF262626)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(paperBgColor)
    ) {
        // Main PDF Page Display with Pinch-to-Zoom & Pan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
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
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1.2f) 1f else 2f
                            offset = Offset.Zero
                        },
                        onTap = {
                            showControls = !showControls
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                            text = "صفحة $currentPage من ${mushaf.totalPages}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Reading Timer pill
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

                    // Paper tint toggle
                    IconButton(onClick = { showPaperModeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "نمط الورق",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Bookmark toggle
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
                            value = currentPage.toFloat(),
                            onValueChange = { viewModel.jumpToPage(it.toInt()) },
                            valueRange = 1f..mushaf.totalPages.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "${mushaf.totalPages}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Navigation buttons row
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

                        // Jump to page text button
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
                            enabled = currentPage < mushaf.totalPages,
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
                        text = "أدخل رقم الصفحة (من 1 إلى ${mushaf.totalPages}):",
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
                            viewModel.jumpToPage(target)
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
}
