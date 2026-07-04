package com.example.rewardwithoutguilt.util

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: Shape,
    on: Dp = 4.dp,
    off: Dp = 4.dp
): Modifier = this.composed {
    val density = LocalDensity.current
    val strokeWidthPx = remember(density, width) { with(density) { width.toPx() } }
    val onPx = remember(density, on) { with(density) { on.toPx() } }
    val offPx = remember(density, off) { with(density) { off.toPx() } }
    
    val pathEffect = remember(onPx, offPx) {
        PathEffect.dashPathEffect(floatArrayOf(onPx, offPx), 0f)
    }
    
    this.drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline = outline,
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                pathEffect = pathEffect,
                cap = StrokeCap.Round
            )
        )
    }
}
