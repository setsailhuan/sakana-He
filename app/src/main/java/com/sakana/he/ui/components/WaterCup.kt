package com.sakana.he.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun WaterCup(
    amount: Int,
    themeColor: Color,
    modifier: Modifier = Modifier,
    onAmountChanged: (Int) -> Unit
) {
    val waterLevel = (amount - 10f) / (300f - 10f)
    val animatedLevel by animateFloatAsState(
        targetValue = waterLevel,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "water_level"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase2"
    )
    val bubbleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_time"
    )

    val bubbles = remember {
        listOf(
            Triple(0.20f, 0.00f, 3.5f),
            Triple(0.45f, 0.25f, 2.5f),
            Triple(0.65f, 0.50f, 4.0f),
            Triple(0.30f, 0.70f, 2.0f),
            Triple(0.75f, 0.10f, 3.0f),
            Triple(0.55f, 0.85f, 2.5f),
            Triple(0.15f, 0.40f, 3.5f),
            Triple(0.80f, 0.60f, 2.0f),
        )
    }

    var boxHeight by remember { mutableIntStateOf(0) }
    var mlAccumulator by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier
        .onSizeChanged { boxHeight = it.height }
        .pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = { mlAccumulator = 0f },
                onDragEnd = { mlAccumulator = 0f },
                onVerticalDrag = { _, dragAmount ->
                    if (boxHeight > 0) {
                        // Cup occupies 82% of box height; map that range to 290ml (10→300)
                        val cupHeightPx = boxHeight * 0.82f
                        val mlPerPixel = 290f / cupHeightPx
                        // drag down → decrease, drag up → increase
                        mlAccumulator -= dragAmount * mlPerPixel
                        val steps = (mlAccumulator / 10f).toInt()
                        if (steps != 0) {
                            mlAccumulator -= steps * 10f
                            onAmountChanged(steps * 10)
                        }
                    }
                }
            )
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cupHeight = size.height * 0.82f
            val cupTop = (size.height - cupHeight) / 2f
            val cupBottom = cupTop + cupHeight
            val cx = size.width / 2f
            val topHalfW = size.width * 0.36f
            val bottomHalfW = size.width * 0.26f

            val cupPath = buildTrapezoidPath(
                cx, cupTop, cupBottom, topHalfW, bottomHalfW,
                topRadius = 18.dp.toPx(), bottomRadius = 12.dp.toPx()
            )

            clipPath(cupPath) {
                drawRect(themeColor.copy(alpha = 0.06f))

                val waterY = cupTop + (cupBottom - cupTop) * (1f - animatedLevel)
                val waveAmp = 10.dp.toPx()
                val waveWidth = topHalfW * 2f

                drawWave(
                    cupLeft = cx - topHalfW, cupRight = cx + topHalfW,
                    cupBottom = cupBottom, waterY = waterY,
                    phase = wavePhase, amplitude = waveAmp,
                    color = themeColor
                )
                drawWave(
                    cupLeft = cx - topHalfW, cupRight = cx + topHalfW,
                    cupBottom = cupBottom, waterY = waterY + waveAmp * 0.6f,
                    phase = wavePhase2 + PI.toFloat() * 0.5f, amplitude = waveAmp * 0.7f,
                    color = themeColor.copy(alpha = 0.55f)
                )

                bubbles.forEach { (relX, phase, radiusDp) ->
                    val bx = (cx - topHalfW) + relX * waveWidth
                    val progress = (bubbleTime + phase) % 1f
                    val by = cupBottom - (cupBottom - waterY - waveAmp * 2) * progress
                    if (by > waterY + waveAmp && progress < 0.95f) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f * (1f - progress * 0.8f)),
                            radius = radiusDp.dp.toPx(),
                            center = Offset(bx, by)
                        )
                    }
                }

                drawRect(
                    color = Color.White.copy(alpha = 0.07f),
                    topLeft = Offset(cx - topHalfW, cupTop),
                    size = Size(waveWidth * 0.12f, cupHeight)
                )
            }

            drawPath(
                path = cupPath,
                color = themeColor.copy(alpha = 0.35f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun buildTrapezoidPath(
    cx: Float, cupTop: Float, cupBottom: Float,
    topHalfW: Float, bottomHalfW: Float,
    topRadius: Float, bottomRadius: Float
): Path {
    val tl = Offset(cx - topHalfW, cupTop)
    val tr = Offset(cx + topHalfW, cupTop)
    val br = Offset(cx + bottomHalfW, cupBottom)
    val bl = Offset(cx - bottomHalfW, cupBottom)

    fun unit(from: Offset, to: Offset): Offset {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy)
        return Offset(dx / len, dy / len)
    }

    val topU = unit(tl, tr)
    val rightU = unit(tr, br)
    val bottomU = unit(br, bl)
    val leftU = unit(bl, tl)

    return Path().apply {
        moveTo(tl.x - topRadius * leftU.x, tl.y - topRadius * leftU.y)
        quadraticBezierTo(tl.x, tl.y, tl.x + topRadius * topU.x, tl.y + topRadius * topU.y)
        lineTo(tr.x - topRadius * topU.x, tr.y - topRadius * topU.y)
        quadraticBezierTo(tr.x, tr.y, tr.x + topRadius * rightU.x, tr.y + topRadius * rightU.y)
        lineTo(br.x - bottomRadius * rightU.x, br.y - bottomRadius * rightU.y)
        quadraticBezierTo(br.x, br.y, br.x + bottomRadius * bottomU.x, br.y + bottomRadius * bottomU.y)
        lineTo(bl.x - bottomRadius * bottomU.x, bl.y - bottomRadius * bottomU.y)
        quadraticBezierTo(bl.x, bl.y, bl.x + bottomRadius * leftU.x, bl.y + bottomRadius * leftU.y)
        close()
    }
}

private fun DrawScope.drawWave(
    cupLeft: Float, cupRight: Float, cupBottom: Float,
    waterY: Float, phase: Float, amplitude: Float, color: Color
) {
    val path = Path()
    val step = 4f
    var x = cupLeft
    path.moveTo(cupLeft, waterY + amplitude * sin(phase.toDouble()).toFloat())
    x += step
    while (x <= cupRight + step) {
        val nx = x.coerceAtMost(cupRight)
        val ny = waterY + amplitude * sin(
            (phase + (nx - cupLeft) / (cupRight - cupLeft) * 2 * PI).toDouble()
        ).toFloat()
        path.lineTo(nx, ny)
        x += step
    }
    path.lineTo(cupRight, cupBottom)
    path.lineTo(cupLeft, cupBottom)
    path.close()
    drawPath(path, color)
}
