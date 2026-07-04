package com.example.rewardwithoutguilt.features

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.PuzzlePreferences
import com.example.rewardwithoutguilt.data.TaskPreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

data class ConfettiParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PuzzleScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val puzzlePrefs = remember { PuzzlePreferences(context) }
    val taskPrefs = remember { TaskPreferences(context) }

    // Loaded puzzle bitmaps mapping: "row_col" -> Bitmap
    val bitmaps = remember { mutableStateMapOf<String, Bitmap>() }
    var isLoadingBitmaps by remember { mutableStateOf(true) }

    // Screen UI Local states (synced from preferences but updated instantly for responsiveness)
    var placedPieces by remember { mutableStateOf(emptySet<String>()) }
    var trayPieces by remember { mutableStateOf(emptyList<String>()) }
    var movesCount by remember { mutableStateOf(0) }
    var timeElapsed by remember { mutableStateOf(0L) }
    var isHintEnabled by remember { mutableStateOf(false) }

    var isPreferencesLoaded by remember { mutableStateOf(false) }
    var wasAlreadyCompleted by remember { mutableStateOf(false) }

    // Selection/Drag interaction state
    var selectedPieceKey by remember { mutableStateOf<String?>(null) }
    var draggedPieceKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Board canvas positioning state
    var boardBounds by remember { mutableStateOf(Rect.Zero) }

    // Snap / Drag handle helper
    val onDropAction = { pieceKey: String, dropPos: Offset ->
        movesCount++
        scope.launch { puzzlePrefs.incrementMoves() }

        val localX = dropPos.x - boardBounds.left
        val localY = dropPos.y - boardBounds.top

        val boardWidth = boardBounds.width
        val boardHeight = boardBounds.height

        if (boardWidth > 0f && boardHeight > 0f && localX in 0f..boardWidth && localY in 0f..boardHeight) {
            val basePieceWidth = boardWidth / 6f
            val basePieceHeight = boardHeight / 6f

            val col = (localX / basePieceWidth).toInt().coerceIn(0, 5)
            val row = (localY / basePieceHeight).toInt().coerceIn(0, 5)

            val correctKey = "${row + 1}_${col + 1}"
            val targetCenterX = boardBounds.left + (col + 0.5f) * basePieceWidth
            val targetCenterY = boardBounds.top + (row + 0.5f) * basePieceHeight

            val distance = kotlin.math.hypot(dropPos.x - targetCenterX, dropPos.y - targetCenterY)
            val snapRadius = basePieceWidth * 0.8f

            if (pieceKey == correctKey || (distance < snapRadius && pieceKey == correctKey)) {
                // Successfully placed!
                placedPieces = placedPieces + pieceKey
                trayPieces = trayPieces - pieceKey
                selectedPieceKey = null
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                scope.launch {
                    puzzlePrefs.saveGameState(
                        placed = placedPieces,
                        tray = trayPieces,
                        moves = movesCount,
                        time = timeElapsed
                    )
                }
            }
        }
    }

    // Load assets
    LaunchedEffect(Unit) {
        val rows = 6
        val cols = 6
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }

        try {
            for (row in 1..rows) {
                for (col in 1..cols) {
                    val key = "${row}_${col}"
                    val assetPath = "images/puzzle_piece_${row}_${col}.png"
                    val inputStream = context.assets.open(assetPath)
                    val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                    if (bitmap != null) {
                        bitmaps[key] = bitmap
                    }
                    inputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingBitmaps = false
        }
    }

    // Load data from preferences
    LaunchedEffect(Unit) {
        puzzlePrefs.placedPieces.collect { placed ->
            puzzlePrefs.trayPieces.collect { tray ->
                puzzlePrefs.movesCount.collect { moves ->
                    puzzlePrefs.timeElapsed.collect { time ->
                        puzzlePrefs.isHintEnabled.collect { hint ->
                            if (!isPreferencesLoaded) {
                                placedPieces = placed
                                trayPieces = tray
                                movesCount = moves
                                timeElapsed = time
                                isHintEnabled = hint
                                isPreferencesLoaded = true

                                if (placed.size == 36) {
                                    wasAlreadyCompleted = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Initialize new game if preferences are empty and loaded
    LaunchedEffect(isPreferencesLoaded, trayPieces, placedPieces) {
        if (isPreferencesLoaded && trayPieces.isEmpty() && placedPieces.isEmpty()) {
            val allKeys = mutableListOf<String>()
            for (row in 1..6) {
                for (col in 1..6) {
                    allKeys.add("${row}_${col}")
                }
            }
            allKeys.shuffle()
            trayPieces = allKeys
            puzzlePrefs.resetGame(allKeys)
        }
    }

    // Timer (Stopwatch) flow
    LaunchedEffect(isPreferencesLoaded, placedPieces.size) {
        while (isPreferencesLoaded && placedPieces.size < 36) {
            delay(1000L)
            timeElapsed++
            puzzlePrefs.updateTimeElapsed(timeElapsed)
        }
    }

    val isVictory = isPreferencesLoaded && placedPieces.size == 36

    // Award XP when victory happens in-game
    LaunchedEffect(isVictory) {
        if (isVictory && !wasAlreadyCompleted) {
            wasAlreadyCompleted = true
            taskPrefs.addXp(100)
        }
    }

    // Main Layout container
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoadingBitmaps || !isPreferencesLoaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Section
                    PuzzleHeader(
                        placedCount = placedPieces.size,
                        movesCount = movesCount,
                        timeElapsed = timeElapsed,
                        isHintEnabled = isHintEnabled,
                        onToggleHint = {
                            isHintEnabled = !isHintEnabled
                            scope.launch { puzzlePrefs.setHintEnabled(isHintEnabled) }
                        },
                        onReset = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val allKeys = mutableListOf<String>()
                            for (row in 1..6) {
                                for (col in 1..6) {
                                    allKeys.add("${row}_${col}")
                                }
                            }
                            allKeys.shuffle()
                            placedPieces = emptySet()
                            trayPieces = allKeys
                            movesCount = 0
                            timeElapsed = 0L
                            wasAlreadyCompleted = false
                            selectedPieceKey = null
                            scope.launch {
                                puzzlePrefs.resetGame(allKeys)
                            }
                        }
                    )

                    // Board Container
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val availableWidth = constraints.maxWidth.toFloat()
                        val availableHeight = constraints.maxHeight.toFloat()

                        // Image proportions (728 x 1279)
                        val originalImageWidth = 728f
                        val originalImageHeight = 1279f
                        val aspectRatio = originalImageWidth / originalImageHeight

                        // Scale the board to fit inside constraints
                        val boardWidth = minOf(availableWidth, availableHeight * aspectRatio)
                        val boardHeight = boardWidth / aspectRatio

                        val basePieceWidth = boardWidth / 6f
                        val basePieceHeight = boardHeight / 6f
                        val tabSize = minOf(basePieceWidth, basePieceHeight) / 3.0f

                        // Jigsaw Board Canvas
                        Card(
                            modifier = Modifier
                                .size(
                                    width = with(LocalDensity.current) { boardWidth.toDp() },
                                    height = with(LocalDensity.current) { boardHeight.toDp() }
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .onGloballyPositioned { coordinates ->
                                    boardBounds = coordinates.boundsInRoot()
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures { tapOffset ->
                                        if (selectedPieceKey != null) {
                                            // Translate root screen coordinate for click
                                            val rootTapPos = boardBounds.topLeft + tapOffset
                                            onDropAction(selectedPieceKey!!, rootTapPos)
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Grid Slots Placeholders / Guide lines
                                val gridPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    alpha = 15 // Very faint
                                    strokeWidth = 1.5f
                                    style = android.graphics.Paint.Style.STROKE
                                }
                                for (i in 1 until 6) {
                                    // Vertical
                                    drawContext.canvas.nativeCanvas.drawLine(i * basePieceWidth, 0f, i * basePieceWidth, boardHeight, gridPaint)
                                    // Horizontal
                                    drawContext.canvas.nativeCanvas.drawLine(0f, i * basePieceHeight, boardWidth, i * basePieceHeight, gridPaint)
                                }

                                val outlinePaint = android.graphics.Paint().apply {
                                    colorFilter = android.graphics.PorterDuffColorFilter(
                                        android.graphics.Color.BLACK,
                                        android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                val strokeWidth = 1.5f
                                val offsets = floatArrayOf(
                                    -strokeWidth, 0f,
                                    strokeWidth, 0f,
                                    0f, -strokeWidth,
                                    0f, strokeWidth
                                )
                                val rectF = RectF()
                                val offsetRectF = RectF()

                                // Iterate 6x6 grid
                                for (row in 0 until 6) {
                                    for (col in 0 until 6) {
                                        val key = "${row + 1}_${col + 1}"
                                        val bitmap = bitmaps[key]

                                        if (bitmap != null) {
                                            // Coordinates matching jigsaw cropping coordinates
                                            val gridLeft = col * basePieceWidth
                                            val gridTop = row * basePieceHeight
                                            val gridRight = (col + 1) * basePieceWidth
                                            val gridBottom = (row + 1) * basePieceHeight

                                            val drawLeft = gridLeft - if (col > 0) tabSize else 0f
                                            val drawTop = gridTop - if (row > 0) tabSize else 0f
                                            val drawRight = gridRight + if (col < 5) tabSize else 0f
                                            val drawBottom = gridBottom + if (row < 5) tabSize else 0f

                                            rectF.set(drawLeft, drawTop, drawRight, drawBottom)

                                            if (placedPieces.contains(key)) {
                                                // 1. Draw Piece outline (shadow effect)
                                                for (i in 0 until offsets.size step 2) {
                                                    val dx = offsets[i]
                                                    val dy = offsets[i + 1]
                                                    offsetRectF.set(rectF.left + dx, rectF.top + dy, rectF.right + dx, rectF.bottom + dy)
                                                    drawContext.canvas.nativeCanvas.drawBitmap(bitmap, null, offsetRectF, outlinePaint)
                                                }
                                                // 2. Draw normal full opacity piece
                                                drawContext.canvas.nativeCanvas.drawBitmap(bitmap, null, rectF, null)
                                            } else if (isHintEnabled) {
                                                // Draw faint hint image
                                                val hintPaint = android.graphics.Paint().apply {
                                                    alpha = 35 // Faint watermark opacity
                                                }
                                                drawContext.canvas.nativeCanvas.drawBitmap(bitmap, null, rectF, hintPaint)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tray Section Title
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remaining Pieces (${trayPieces.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (selectedPieceKey != null) {
                            Text(
                                text = "Select a spot on the board to place",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Scrollable pieces Tray
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(items = trayPieces, key = { it }) { pieceKey ->
                            val isSelected = selectedPieceKey == pieceKey
                            val parts = pieceKey.split("_")
                            val row = parts[0].toInt()
                            val col = parts[1].toInt()

                            var itemBounds by remember { mutableStateOf(Rect.Zero) }

                            val borderBrush = if (isSelected) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(115.dp)
                                    .onGloballyPositioned { itemBounds = it.boundsInRoot() }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        brush = borderBrush,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .pointerInput(pieceKey) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { localOffset ->
                                                draggedPieceKey = pieceKey
                                                dragOffset = itemBounds.topLeft + localOffset
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount
                                            },
                                            onDragEnd = {
                                                onDropAction(pieceKey, dragOffset)
                                                draggedPieceKey = null
                                            },
                                            onDragCancel = {
                                                draggedPieceKey = null
                                            }
                                        )
                                    }
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedPieceKey = if (isSelected) null else pieceKey
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                val bitmap = bitmaps[pieceKey]
                                if (bitmap != null) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    ) {
                                        val bmpWidth = bitmap.width.toFloat()
                                        val bmpHeight = bitmap.height.toFloat()
                                        val scale = minOf(size.width / bmpWidth, size.height / bmpHeight)
                                        val drawWidth = bmpWidth * scale
                                        val drawHeight = bmpHeight * scale
                                        val left = (size.width - drawWidth) / 2f
                                        val top = (size.height - drawHeight) / 2f

                                        drawContext.canvas.nativeCanvas.drawBitmap(
                                            bitmap,
                                            null,
                                            RectF(left, top, left + drawWidth, top + drawHeight),
                                            null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Drag and Drop Floating Piece Overlay
                if (draggedPieceKey != null) {
                    val bitmap = bitmaps[draggedPieceKey!!]
                    if (bitmap != null) {
                        // Approximate size equivalent to board piece size for scaling reference
                        val parts = draggedPieceKey!!.split("_")
                        val row = parts[0].toInt() - 1
                        val col = parts[1].toInt() - 1

                        val availableWidth = boardBounds.width
                        val basePieceWidth = availableWidth / 6f
                        val basePieceHeight = boardBounds.height / 6f
                        val tabSize = minOf(basePieceWidth, basePieceHeight) / 3.0f

                        val width = basePieceWidth + (if (col > 0) tabSize else 0f) + (if (col < 5) tabSize else 0f)
                        val height = basePieceHeight + (if (row > 0) tabSize else 0f) + (if (row < 5) tabSize else 0f)

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (dragOffset.x - width / 2).toInt(),
                                        (dragOffset.y - height / 2).toInt()
                                    )
                                }
                                .size(
                                    width = with(LocalDensity.current) { width.toDp() },
                                    height = with(LocalDensity.current) { height.toDp() }
                                )
                                .graphicsLayer(
                                    scaleX = 1.15f,
                                    scaleY = 1.15f,
                                    alpha = 0.85f,
                                    shadowElevation = with(LocalDensity.current) { 8.dp.toPx() },
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawContext.canvas.nativeCanvas.drawBitmap(
                                    bitmap,
                                    null,
                                    RectF(0f, 0f, size.width, size.height),
                                    null
                                )
                            }
                        }
                    }
                }

                // Victory Celebration Overlay
                AnimatedVisibility(
                    visible = isVictory,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut(targetScale = 0.9f)
                ) {
                    ConfettiVictoryOverlay(
                        timeString = formatTime(timeElapsed),
                        moves = movesCount,
                        onPlayAgain = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val allKeys = mutableListOf<String>()
                            for (row in 1..6) {
                                for (col in 1..6) {
                                    allKeys.add("${row}_${col}")
                                }
                            }
                            allKeys.shuffle()
                            placedPieces = emptySet()
                            trayPieces = allKeys
                            movesCount = 0
                            timeElapsed = 0L
                            wasAlreadyCompleted = false
                            selectedPieceKey = null
                            scope.launch {
                                puzzlePrefs.resetGame(allKeys)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PuzzleHeader(
    placedCount: Int,
    movesCount: Int,
    timeElapsed: Long,
    isHintEnabled: Boolean,
    onToggleHint: () -> Unit,
    onReset: () -> Unit
) {
    val progress = placedCount.toFloat() / 36f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Puzzle Challenge",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Drag pieces to solve or tap to place",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Hint Button
                IconButton(
                    onClick = onToggleHint,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isHintEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Hint",
                        tint = if (isHintEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reset Button
                IconButton(
                    onClick = onReset,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Board",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatTime(timeElapsed),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Progress Info
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "COMPLETED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$placedCount / 36",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Moves Counter
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MOVES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$movesCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ConfettiVictoryOverlay(
    timeString: String,
    moves: Int,
    onPlayAgain: () -> Unit
) {
    var particles by remember { mutableStateOf(emptyList<ConfettiParticle>()) }
    val random = remember { Random() }
    val density = LocalDensity.current

    // Confetti particles loop
    LaunchedEffect(Unit) {
        val colors = listOf(
            Color(0xFFD0BCFF), // Purple
            Color(0xFFEFB8C8), // Pink
            Color(0xFF6650a4), // Indigo
            Color(0xFF6366F1), // Violet
            Color(0xFFFFD700), // Gold
            Color(0xFF3B82F6), // Blue
            Color(0xFF10B981)  // Emerald
        )
        
        // Spawn 80 particles
        particles = List(80) { i ->
            ConfettiParticle(
                id = i,
                x = random.nextFloat() * 1500f,
                y = -random.nextFloat() * 400f,
                speedX = (random.nextFloat() - 0.5f) * 6f,
                speedY = 6f + random.nextFloat() * 8f,
                color = colors[random.nextInt(colors.size)],
                size = 12f + random.nextFloat() * 16f,
                rotation = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 8f
            )
        }

        while (true) {
            particles = particles.map { p ->
                val nextY = p.y + p.speedY
                p.copy(
                    x = p.x + p.speedX,
                    y = if (nextY > 2500f) -50f else nextY,
                    rotation = (p.rotation + p.rotationSpeed) % 360f
                )
            }
            delay(16L) // ~60FPS
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    drawRoundRect(
                        color = p.color,
                        topLeft = Offset(p.x - p.size / 2, p.y - p.size / 4),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size / 2),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                }
            }
        }

        // Victory Card
        Card(
            modifier = Modifier
                .width(320.dp)
                .padding(24.dp)
                .graphicsLayer(
                    shadowElevation = with(density) { 24.dp.toPx() },
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎉",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Puzzle Solved!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Congratulations! You successfully assembled all pieces of the puzzle.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = timeString,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MOVES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$moves",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reward alert
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⭐ Earned +100 XP!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Play Again",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

@Preview(showBackground = true)
@Composable
fun PuzzleScreenPreview() {
    RewardWithoutGuiltTheme {
        PuzzleScreen()
    }
}
