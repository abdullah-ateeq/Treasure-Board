package com.example.treasureboard.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.treasureboard.game.Player
import com.example.treasureboard.game.SpaceType
import com.example.treasureboard.ui.board.drawTile
import com.example.treasureboard.ui.board.drawToken
import com.example.treasureboard.ui.components.DieView
import com.example.treasureboard.ui.components.OrnamentDivider
import com.example.treasureboard.ui.components.ScreenBackground
import com.example.treasureboard.ui.theme.ClassicBoard
import com.example.treasureboard.ui.theme.GoldLight
import kotlin.math.PI
import kotlin.math.sin

private const val SPLASH_MS = 1500

private val SPLASH_LOOP = listOf(
    SpaceType.START, SpaceType.COIN, SpaceType.NORMAL, SpaceType.TREASURE,
    SpaceType.NORMAL, SpaceType.TRAP, SpaceType.COIN, SpaceType.GOLD_CHEST,
    SpaceType.NORMAL, SpaceType.BONUS_STEP, SpaceType.COIN, SpaceType.NORMAL
)

/** Clockwise cells of a 4×4 perimeter, starting bottom-left. */
private val SPLASH_CELLS: List<Pair<Int, Int>> = buildList {
    for (row in 3 downTo 1) add(0 to row)
    for (col in 0 until 3) add(col to 0)
    for (row in 0 until 3) add(3 to row)
    for (col in 3 downTo 1) add(col to 3)
}

private fun phase(t: Float, start: Float, end: Float) = ((t - start) / (end - start)).coerceIn(0f, 1f)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        // Wall-clock bound so a slow first frame never holds the splash longer than intended.
        launch { progress.animateTo(1f, tween(SPLASH_MS, easing = LinearEasing)) }
        delay(SPLASH_MS + 150L)
        finish()
    }
    val t = progress.value
    val boardAlpha = phase(t, 0f, 0.2f)
    val dieIn = phase(t, 0.1f, 0.2f)
    val dieOut = 1f - phase(t, 0.5f, 0.6f)
    val rolling = t < 0.36f
    val dieFace = if (rolling) (((t * 40).toInt()) % 6) + 1 else 3
    val tokenProgress = phase(t, 0.36f, 0.58f) * 3f
    val chestIn = phase(t, 0.52f, 0.62f)
    val lidOpen = phase(t, 0.6f, 0.76f)
    val pointsT = phase(t, 0.68f, 0.92f)
    val titleAlpha = phase(t, 0.62f, 0.86f)

    ScreenBackground {
        Column(
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Treasure Board" },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .alpha(boardAlpha)
                ) { drawSplashLoop(tokenProgress) }

                DieView(
                    face = dieFace,
                    rolling = rolling,
                    size = 64.dp,
                    modifier = Modifier.graphicsLayer {
                        alpha = dieIn * dieOut
                        scaleX = 0.8f + 0.2f * dieIn
                        scaleY = 0.8f + 0.2f * dieIn
                    }
                )

                Canvas(
                    Modifier
                        .size(96.dp)
                        .alpha(chestIn)
                ) { drawOpeningChest(lidOpen) }

                Text(
                    "+8",
                    color = GoldLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .offset(y = (-46 - 18 * pointsT).dp)
                        .alpha(if (pointsT <= 0f) 0f else sin(PI * pointsT).toFloat())
                )
            }
            Spacer(Modifier.height(28.dp))
            Column(Modifier.alpha(titleAlpha), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "TREASURE BOARD",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                OrnamentDivider()
            }
        }
    }
}

private fun DrawScope.drawSplashLoop(tokenProgress: Float) {
    val cell = size.minDimension / 4f
    fun centerOf(i: Int): Offset {
        val (col, row) = SPLASH_CELLS[i % SPLASH_CELLS.size]
        return Offset((col + 0.5f) * cell, (row + 0.5f) * cell)
    }
    SPLASH_LOOP.forEachIndexed { i, type ->
        val next = centerOf(i + 1) - centerOf(i)
        drawTile(type, centerOf(i), cell, ClassicBoard, next, label = null)
    }
    val stepIndex = tokenProgress.toInt().coerceAtMost(3)
    val frac = tokenProgress - stepIndex
    val from = centerOf(stepIndex)
    val to = centerOf((stepIndex + 1).coerceAtMost(3))
    val pos = if (stepIndex >= 3) centerOf(3) else from + (to - from) * frac
    val lift = if (stepIndex >= 3) 0f else sin(PI * frac).toFloat()
    drawToken(Player.PLAYER_ONE, pos, cell * 0.28f, lift)
}

private fun DrawScope.drawOpeningChest(open: Float) {
    val w = size.minDimension
    val c = Offset(w / 2, w * 0.6f)
    val bodyW = w * 0.62f
    val bodyH = w * 0.3f
    val left = c.x - bodyW / 2
    val seam = c.y - bodyH * 0.2f
    val palette = ClassicBoard

    drawCircle(
        Brush.radialGradient(listOf(GoldLight.copy(alpha = 0.7f * open), Color.Transparent), Offset(c.x, seam), w * 0.5f),
        w * 0.5f,
        Offset(c.x, seam)
    )
    drawRoundRect(palette.chestWood, Offset(left, seam), Size(bodyW, bodyH), CornerRadius(w * 0.03f))
    drawRect(palette.chestGold, Offset(left, seam), Size(bodyW, w * 0.035f))
    listOf(0.22f, 0.78f).forEach { f ->
        drawRect(palette.chestGold, Offset(left + bodyW * f - w * 0.025f, seam), Size(w * 0.05f, bodyH))
    }
    drawRoundRect(palette.chestGold, Offset(c.x - w * 0.05f, seam), Size(w * 0.1f, w * 0.1f), CornerRadius(w * 0.02f))

    // Lid hinges at the back edge and tilts open.
    rotate(-70f * open, pivot = Offset(left + bodyW, seam)) {
        val lid = Path().apply {
            moveTo(left, seam)
            lineTo(left, seam - w * 0.08f)
            cubicTo(left, seam - w * 0.24f, left + bodyW, seam - w * 0.24f, left + bodyW, seam - w * 0.08f)
            lineTo(left + bodyW, seam)
            close()
        }
        drawPath(lid, palette.chestWood)
        drawRect(palette.chestGold, Offset(left, seam - w * 0.035f), Size(bodyW, w * 0.035f))
    }
}
