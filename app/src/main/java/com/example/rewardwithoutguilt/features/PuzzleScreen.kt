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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random


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
    var placedPieces by remember { mutableStateOf(emptyMap<String, String>()) }
    var trayPieces by remember { mutableStateOf(emptyList<String>()) }
    var isHintEnabled by remember { mutableStateOf(false) }

    var isPreferencesLoaded by remember { mutableStateOf(false) }
    var wasAlreadyCompleted by remember { mutableStateOf(false) }

    // Selection/Drag interaction state
    var selectedPieceKey by remember { mutableStateOf<String?>(null) }
    var draggedPieceKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Board canvas positioning state
    var boardBounds by remember { mutableStateOf(Rect.Zero) }
    val correctPieceGlows = remember { mutableStateMapOf<String, Animatable<Float, AnimationVector1D>>() }

    // Snap / Drag handle helper defined at screen level
    val onDropAction = { pieceKey: String, dropPos: Offset ->
        val localX = dropPos.x - boardBounds.left
        val localY = dropPos.y - boardBounds.top

        val boardWidth = boardBounds.width
        val boardHeight = boardBounds.height

        if (boardWidth > 0f && boardHeight > 0f && localX in 0f..boardWidth && localY in 0f..boardHeight) {
            val basePieceWidth = boardWidth / 6f
            val basePieceHeight = boardHeight / 6f

            val col = (localX / basePieceWidth).toInt().coerceIn(0, 5)
            val row = (localY / basePieceHeight).toInt().coerceIn(0, 5)

            val slotKey = "${row + 1}_${col + 1}"
            
            // Check if slot is empty
            if (!placedPieces.containsKey(slotKey)) {
                // Successfully placed on the board!
                placedPieces = placedPieces + (slotKey to pieceKey)
                trayPieces = trayPieces - pieceKey
                selectedPieceKey = null
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                if (slotKey == pieceKey) {
                    val animatable = Animatable(0f)
                    correctPieceGlows[slotKey] = animatable
                    scope.launch {
                        animatable.animateTo(1f, tween(400))
                        animatable.animateTo(0.2f, tween(400))
                        animatable.animateTo(1f, tween(400))
                        animatable.animateTo(0f, tween(400))
                    }
                }

                scope.launch {
                    puzzlePrefs.saveGameState(
                        placed = placedPieces,
                        tray = trayPieces
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
                puzzlePrefs.isHintEnabled.collect { hint ->
                    if (!isPreferencesLoaded) {
                        placedPieces = placed
                        trayPieces = tray
                        isHintEnabled = hint
                        isPreferencesLoaded = true

                        if (placed.size == 36 && placed.all { it.key == it.value }) {
                            wasAlreadyCompleted = true
                        }
                    }
                }
            }
        }
        scope.launch { puzzlePrefs.clearUnseenEarnedPieces() }
    }



    val isVictory = isPreferencesLoaded && placedPieces.size == 36 && placedPieces.all { it.key == it.value }
    val sweepProgress = remember { Animatable(0f) }



    // Award XP when victory happens in-game
    LaunchedEffect(isVictory) {
        if (isVictory && !wasAlreadyCompleted) {
            wasAlreadyCompleted = true
            taskPrefs.addXp(100)
            delay(500) // Wait for shrink animations
            sweepProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )
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
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Section
                    AnimatedVisibility(
                        visible = !isVictory,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        PuzzleHeader(
                            placedCount = placedPieces.size
                        )
                    }

                    // Board Container
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp),
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
                                            val rootTapPos = boardBounds.topLeft + tapOffset
                                            onDropAction(selectedPieceKey!!, rootTapPos)
                                        } else if (!isVictory) {
                                            val boardWidth = boardBounds.width
                                            val boardHeight = boardBounds.height
                                            if (boardWidth > 0f && boardHeight > 0f) {
                                                val col = (tapOffset.x / (boardWidth / 6f)).toInt().coerceIn(0, 5)
                                                val row = (tapOffset.y / (boardHeight / 6f)).toInt().coerceIn(0, 5)
                                                val slotKey = "${row + 1}_${col + 1}"
                                                
                                                val pieceKey = placedPieces[slotKey]
                                                if (pieceKey != null) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    placedPieces = placedPieces - slotKey
                                                    trayPieces = trayPieces + pieceKey
                                                    selectedPieceKey = pieceKey // Automatically select the removed piece
                                                    scope.launch {
                                                        puzzlePrefs.saveGameState(placedPieces, trayPieces)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Grid Slots Placeholders / Guide lines
                                if (!isVictory) {
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
                                        val slotKey = "${row + 1}_${col + 1}"
                                        val placedPieceKey = placedPieces[slotKey]

                                        val gridLeft = col * basePieceWidth
                                        val gridTop = row * basePieceHeight
                                        val gridRight = (col + 1) * basePieceWidth
                                        val gridBottom = (row + 1) * basePieceHeight

                                        if (placedPieceKey != null) {
                                            val bitmap = bitmaps[placedPieceKey]
                                            if (bitmap != null) {
                                                val pParts = placedPieceKey.split("_")
                                                val pRow = pParts[0].toInt() - 1
                                                val pCol = pParts[1].toInt() - 1

                                                val drawLeft = gridLeft - if (pCol > 0) tabSize else 0f
                                                val drawTop = gridTop - if (pRow > 0) tabSize else 0f
                                                val drawRight = gridRight + if (pCol < 5) tabSize else 0f
                                                val drawBottom = gridBottom + if (pRow < 5) tabSize else 0f

                                                rectF.set(drawLeft, drawTop, drawRight, drawBottom)

                                                if (!isVictory) {
                                                    val isCorrect = placedPieceKey == slotKey
                                                    val glowAlphaValue = correctPieceGlows[slotKey]?.value ?: 0f
                                                    val activePaint = if (isCorrect && glowAlphaValue > 0f) {
                                                        android.graphics.Paint().apply {
                                                            colorFilter = android.graphics.PorterDuffColorFilter(
                                                                android.graphics.Color.GREEN,
                                                                android.graphics.PorterDuff.Mode.SRC_IN
                                                            )
                                                            alpha = (glowAlphaValue * 255).toInt()
                                                        }
                                                    } else outlinePaint
                                                    
                                                    // Draw outline (shadow or green glow)
                                                    for (i in 0 until offsets.size step 2) {
                                                        val dx = offsets[i]
                                                        val dy = offsets[i + 1]
                                                        offsetRectF.set(rectF.left + dx, rectF.top + dy, rectF.right + dx, rectF.bottom + dy)
                                                        drawContext.canvas.nativeCanvas.drawBitmap(bitmap, null, offsetRectF, activePaint)
                                                    }
                                                }
                                                // Draw normal full opacity piece
                                                drawContext.canvas.nativeCanvas.drawBitmap(bitmap, null, rectF, null)
                                            }
                                        } else if (isHintEnabled) {
                                            val bitmap = bitmaps[slotKey]
                                            if (bitmap != null) {
                                                val drawLeft = gridLeft - if (col > 0) tabSize else 0f
                                                val drawTop = gridTop - if (row > 0) tabSize else 0f
                                                val drawRight = gridRight + if (col < 5) tabSize else 0f
                                                val drawBottom = gridBottom + if (row < 5) tabSize else 0f
                                                
                                                rectF.set(drawLeft, drawTop, drawRight, drawBottom)
                                                
                                                // Draw faint hint image
                                                val hintPaint = android.graphics.Paint().apply {
                                                    alpha = 35 // Faint watermark opacity
                                                }
                                                drawContext.canvas.nativeCanvas.drawBitmap(bitmap, null, rectF, hintPaint)
                                            }
                                        }
                                    }
                                }

                                // Draw sweep animation
                                val progress = sweepProgress.value
                                if (progress > 0f && progress < 1f) {
                                    val w = size.width
                                    val h = size.height
                                    val flashWidth = w * 0.5f
                                    val startX = -flashWidth + (w + flashWidth) * progress * 1.5f
                                    
                                    val brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.6f),
                                            Color.Transparent
                                        ),
                                        start = Offset(startX, 0f),
                                        end = Offset(startX + flashWidth, h)
                                    )
                                    drawRect(brush = brush, size = size)
                                }
                            }
                        }
                    }

                    // Tray Section Title & Actions
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = if (isVictory) Arrangement.Center else Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = !isVictory,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "Earned Pieces (${trayPieces.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Debug Button
                            IconButton(
                                onClick = {
                                    val allKeysMap = mutableMapOf<String, String>()
                                    for (row in 1..6) {
                                        for (col in 1..6) {
                                            val key = "${row}_${col}"
                                            allKeysMap[key] = key
                                        }
                                    }
                                    placedPieces = allKeysMap
                                    trayPieces = emptyList()
                                    selectedPieceKey = null
                                    scope.launch {
                                        puzzlePrefs.saveGameState(placedPieces, trayPieces)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Auto Complete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val allKeys = mutableListOf<String>()
                                    for (row in 1..6) {
                                        for (col in 1..6) {
                                            allKeys.add("${row}_${col}")
                                        }
                                    }
                                    allKeys.shuffle()
                                    placedPieces = emptyMap()
                                    trayPieces = emptyList()
                                    wasAlreadyCompleted = false
                                    selectedPieceKey = null
                                    scope.launch {
                                        puzzlePrefs.resetGame(allKeys)
                                        sweepProgress.snapTo(0f)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Redo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    isHintEnabled = !isHintEnabled
                                    scope.launch { puzzlePrefs.setHintEnabled(isHintEnabled) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Preview",
                                    tint = if (isHintEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !isVictory && selectedPieceKey != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = "Select a spot on the board to place",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 2.dp)
                        )
                    }

                    // Scrollable pieces Tray
                    AnimatedVisibility(
                        visible = !isVictory,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(top = 4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                    .width(60.dp)
                                    .height(86.dp)
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
                }

                // Drag and Drop Floating Piece Overlay
                if (draggedPieceKey != null) {
                    val bitmap = bitmaps[draggedPieceKey!!]
                    if (bitmap != null) {
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

            }
        }
    }
}

@Composable
fun PuzzleHeader(
    placedCount: Int
) {
    val progress = placedCount.toFloat() / 36f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Progress Text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = "$placedCount / 36 pieces placed",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PuzzleScreenPreview() {
    RewardWithoutGuiltTheme {
        PuzzleScreen()
    }
}
