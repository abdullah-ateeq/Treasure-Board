package com.example.treasureboard.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.treasureboard.ScorePopup
import com.example.treasureboard.game.Board
import com.example.treasureboard.game.Player
import com.example.treasureboard.game.TreasureBoardState
import com.example.treasureboard.game.description
import com.example.treasureboard.ui.theme.BoardPalette
import com.example.treasureboard.ui.theme.Forest950
import com.example.treasureboard.ui.theme.GoldLight
import com.example.treasureboard.ui.theme.colors
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TreasureBoardView(
    board: Board,
    game: TreasureBoardState,
    palette: BoardPalette,
    showLabels: Boolean,
    stepMillis: Int,
    popup: ScorePopup?,
    modifier: Modifier = Modifier,
    center: @Composable BoxScope.() -> Unit = {}
) {
    BoxWithConstraints(modifier.aspectRatio(1f)) {
        val boardDp = maxWidth
        val cellDp = boardDp / Board.GRID

        BoardTiles(board, palette, showLabels, Modifier.fillMaxSize())

        Box(
            Modifier
                .align(Alignment.Center)
                .size(cellDp * 5.6f),
            contentAlignment = Alignment.Center,
            content = center
        )

        BoardTokens(board, game, stepMillis, Modifier.fillMaxSize())

        popup?.let { key(it.id) { ScorePopupLabel(board, it) } }

        // Invisible per-space nodes so screen readers can explore the board.
        board.spaces.forEach { space ->
            val occupants = buildList {
                if (game.player1.position == space.id) add("Player 1 token here")
                if (game.player2.position == space.id) add("Player 2 token here")
            }
            Box(
                Modifier
                    .offset(x = boardDp * space.x - cellDp / 2, y = boardDp * space.y - cellDp / 2)
                    .size(cellDp)
                    .semantics {
                        contentDescription = buildString {
                            append("Space ${space.id}, ${space.type.description}")
                            occupants.forEach { append(". ").append(it) }
                        }
                    }
            )
        }
    }
}

@Composable
private fun BoardTiles(board: Board, palette: BoardPalette, showLabels: Boolean, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    Spacer(
        modifier.drawWithCache {
            val w = size.minDimension
            val cell = w / Board.GRID
            val labelStyle = TextStyle(
                color = palette.label,
                fontSize = (cell * 0.19f / density).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            val startStyle = labelStyle.copy(
                color = palette.start,
                fontSize = (cell * 0.17f / density).sp,
                letterSpacing = 0.8.sp
            )
            val labels = board.spaces.map { space ->
                val text = space.type.shortLabel() ?: return@map null
                val isStart = space.type == com.example.treasureboard.game.SpaceType.START
                if (!showLabels && !isStart) null
                else measurer.measure(text, if (isStart) startStyle else labelStyle)
            }
            onDrawBehind {
                drawBoardBase(board, palette)
                board.spaces.forEachIndexed { i, space ->
                    drawTile(
                        type = space.type,
                        center = Offset(space.x * w, space.y * w),
                        cell = cell,
                        palette = palette,
                        direction = board.directionAt(i),
                        label = labels[i]
                    )
                }
            }
        }
    )
}

private data class TokenMotion(val center: Offset, val lift: Float)

@Composable
private fun rememberTokenMotion(board: Board, position: Int, stepMillis: Int): TokenMotion {
    val space = board[position]
    val target by animateOffsetAsState(
        targetValue = Offset(space.x, space.y),
        animationSpec = tween((stepMillis * 0.85f).toInt(), easing = FastOutSlowInEasing),
        label = "token"
    )
    val hop = remember { Animatable(0f) }
    var lastPosition by remember { mutableIntStateOf(position) }
    LaunchedEffect(position) {
        if (position != lastPosition) {
            lastPosition = position
            hop.snapTo(0f)
            hop.animateTo(1f, tween(stepMillis, easing = LinearEasing))
            hop.snapTo(0f)
        }
    }
    return TokenMotion(target, sin(PI * hop.value).toFloat())
}

@Composable
private fun BoardTokens(board: Board, game: TreasureBoardState, stepMillis: Int, modifier: Modifier) {
    val p1 = rememberTokenMotion(board, game.player1.position, stepMillis)
    val p2 = rememberTokenMotion(board, game.player2.position, stepMillis)
    val shared by animateFloatAsState(
        targetValue = if (game.player1.position == game.player2.position) 1f else 0f,
        animationSpec = tween(160),
        label = "shared"
    )
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowing = game.waitingForRoll && !game.isGameOver
    val description = "Player 1 token, space ${game.player1.position}. " +
        "Player 2 token, space ${game.player2.position}."

    Canvas(modifier.semantics { contentDescription = description }) {
        val w = size.minDimension
        val cell = w / Board.GRID
        val radius = cell * 0.27f
        val spread = cell * 0.17f * shared
        fun draw(player: Player, motion: TokenMotion, sign: Float) {
            drawToken(
                player = player,
                center = Offset(motion.center.x * w + spread * sign, motion.center.y * w + spread * sign * 0.6f),
                radius = radius,
                lift = motion.lift,
                glow = if (glowing && game.currentPlayer == player) pulse else 0f
            )
        }
        // The active player's token is drawn last so it sits on top.
        if (game.currentPlayer == Player.PLAYER_ONE) {
            draw(Player.PLAYER_TWO, p2, 1f); draw(Player.PLAYER_ONE, p1, -1f)
        } else {
            draw(Player.PLAYER_ONE, p1, -1f); draw(Player.PLAYER_TWO, p2, 1f)
        }
    }
}

@Composable
private fun ScorePopupLabel(board: Board, popup: ScorePopup) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1400, easing = LinearEasing)) }
    val t = progress.value
    val alpha = when {
        t < 0.12f -> t / 0.12f
        t > 0.7f -> (1f - t) / 0.3f
        else -> 1f
    }
    val space = board[popup.spaceId]
    val accent = if (popup.positive) GoldLight else Color(0xFFE59A84)

    Text(
        text = popup.text,
        color = accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                val w = constraints.maxWidth
                val cell = w / Board.GRID.toFloat()
                val rise = t * cell * 0.7f
                val x = (space.x * w - placeable.width / 2f).coerceIn(0f, (w - placeable.width).toFloat())
                val y = (space.y * w - cell * 0.75f - placeable.height / 2f - rise)
                    .coerceIn(0f, (constraints.maxHeight - placeable.height).toFloat())
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place(x.roundToInt(), y.roundToInt())
                }
            }
            .graphicsLayer { this.alpha = alpha }
            .background(Forest950.copy(alpha = 0.88f), RoundedCornerShape(8.dp))
            .border(1.dp, popup.player.colors.light.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
