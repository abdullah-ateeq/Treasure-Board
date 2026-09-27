package com.example.treasureboard.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.treasureboard.GameEvent
import com.example.treasureboard.GameUiState
import com.example.treasureboard.GameViewModel
import com.example.treasureboard.audio.Haptic
import com.example.treasureboard.audio.Haptics
import com.example.treasureboard.audio.Sound
import com.example.treasureboard.audio.SoundPlayer
import com.example.treasureboard.data.AppSettings
import com.example.treasureboard.game.Board
import com.example.treasureboard.game.Player
import com.example.treasureboard.game.SpaceType
import com.example.treasureboard.game.TreasurePlayerState
import com.example.treasureboard.game.TreasureRules
import com.example.treasureboard.ui.board.TreasureBoardView
import com.example.treasureboard.ui.components.DieView
import com.example.treasureboard.ui.components.GoldButton
import com.example.treasureboard.ui.components.PlayerSymbol
import com.example.treasureboard.ui.components.ScreenBackground
import com.example.treasureboard.ui.components.TbIcons
import com.example.treasureboard.ui.theme.GoldLight
import com.example.treasureboard.ui.theme.colors
import com.example.treasureboard.ui.theme.palette
import kotlinx.coroutines.delay

private const val RESULT_DELAY_MS = 1400L

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: AppSettings,
    onHome: () -> Unit,
    onGameOver: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val game = ui.game
    var showRestart by rememberSaveable { mutableStateOf(false) }
    var showLeave by rememberSaveable { mutableStateOf(false) }

    GameFeedback(viewModel, settings)

    val finish by rememberUpdatedState(onGameOver)
    LaunchedEffect(game.isGameOver) {
        if (game.isGameOver) {
            delay(RESULT_DELAY_MS)
            finish()
        }
    }

    val inProgress = game.turns > 0 && !game.isGameOver
    val requestHome = { if (inProgress) showLeave = true else onHome() }
    BackHandler(onBack = requestHome)

    val canRoll = game.waitingForRoll && !game.isAnimating && !game.isGameOver
    val stepMillis = (if (settings.fastMovement) GameViewModel.FAST_STEP_MS else GameViewModel.STEP_MS).toInt()

    ScreenBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            val board: @Composable (Modifier) -> Unit = { mod ->
                TreasureBoardView(
                    board = viewModel.board,
                    game = game,
                    palette = settings.boardTheme.palette,
                    showLabels = settings.showSpaceLabels,
                    stepMillis = stepMillis,
                    popup = ui.popup,
                    modifier = mod
                ) { BoardCenter(ui) }
            }
            val topBar: @Composable () -> Unit = {
                GameTopBar(
                    ui = ui,
                    onHome = requestHome,
                    onRestart = { if (game.turns > 0) showRestart = true },
                    restartEnabled = game.turns > 0 && !game.isGameOver
                )
            }
            val rollButton: @Composable () -> Unit = {
                GoldButton(
                    text = if (game.isGameOver) "Race complete" else "Roll",
                    onClick = viewModel::rollDice,
                    enabled = canRoll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = if (canRoll) "Roll the die for ${game.currentPlayer.label}" else "Roll unavailable"
                        }
                )
            }

            if (landscape) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.fillMaxHeight().weight(1.2f), contentAlignment = Alignment.Center) {
                        board(Modifier)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                    ) {
                        topBar()
                        PlayerPanel(Player.PLAYER_TWO, game.player2, ui)
                        PlayerPanel(Player.PLAYER_ONE, game.player1, ui)
                        rollButton()
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    topBar()
                    PlayerPanel(Player.PLAYER_TWO, game.player2, ui, Modifier.widthIn(max = 560.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { board(Modifier) }
                    PlayerPanel(Player.PLAYER_ONE, game.player1, ui, Modifier.widthIn(max = 560.dp))
                    Box(Modifier.widthIn(max = 560.dp).padding(bottom = 4.dp)) { rollButton() }
                }
            }
        }
    }

    if (showRestart) {
        ConfirmDialog(
            title = "Restart this treasure race?",
            body = "Scores and positions for both players will be cleared.",
            confirm = "Restart",
            onConfirm = { showRestart = false; viewModel.newGame() },
            onDismiss = { showRestart = false }
        )
    }
    if (showLeave) {
        ConfirmDialog(
            title = "Leave this treasure race?",
            body = "The current match will not be saved.",
            confirm = "Leave",
            onConfirm = { showLeave = false; onHome() },
            onDismiss = { showLeave = false }
        )
    }
}

/** Plays sounds and haptics for game events, honouring the current settings. */
@Composable
private fun GameFeedback(viewModel: GameViewModel, settings: AppSettings) {
    val view = LocalView.current
    val haptics = remember(view) { Haptics(view) }
    val sounds = remember { SoundPlayer() }
    DisposableEffect(sounds) { onDispose { sounds.release() } }
    val current by rememberUpdatedState(settings)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val sound = current.soundEnabled
            val haptic = current.hapticsEnabled
            fun play(s: Sound) { if (sound) sounds.play(s) }
            fun buzz(h: Haptic) { if (haptic) haptics.perform(h) }
            when (event) {
                GameEvent.DiceRolled -> { play(Sound.DICE); buzz(Haptic.VERY_LIGHT) }
                GameEvent.TokenStep -> play(Sound.MOVE)
                GameEvent.BonusMove -> { play(Sound.BONUS); buzz(Haptic.VERY_LIGHT) }
                is GameEvent.Landed -> when (event.effect.type) {
                    SpaceType.COIN -> { play(Sound.COIN); buzz(Haptic.VERY_LIGHT) }
                    SpaceType.TREASURE -> { play(Sound.TREASURE); buzz(Haptic.SUCCESS) }
                    SpaceType.GOLD_CHEST -> { play(Sound.GOLD); buzz(Haptic.STRONG_SUCCESS) }
                    SpaceType.TRAP -> { play(Sound.TRAP); buzz(Haptic.WARNING) }
                    SpaceType.START, SpaceType.NORMAL, SpaceType.BONUS_STEP -> Unit
                }
                is GameEvent.GameOver -> { play(Sound.WIN); buzz(Haptic.WIN) }
            }
        }
    }
}

@Composable
private fun GameTopBar(ui: GameUiState, onHome: () -> Unit, onRestart: () -> Unit, restartEnabled: Boolean) {
    val game = ui.game
    val onBg = MaterialTheme.colorScheme.onBackground
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onHome) { Icon(TbIcons.Home, contentDescription = "Home", tint = onBg) }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { TurnIndicator(ui) }
        IconButton(onClick = onRestart, enabled = restartEnabled) {
            Icon(
                TbIcons.Restart,
                contentDescription = "Restart",
                tint = if (restartEnabled) onBg else onBg.copy(alpha = 0.3f)
            )
        }
    }
    if (game.finishTriggeredBy != null && !game.isGameOver) {
        Text(
            "FINAL TURN · ${game.finishTriggeredBy.label.uppercase()} COMPLETED 2 LAPS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TurnIndicator(ui: GameUiState) {
    val game = ui.game
    val text = when {
        game.isGameOver -> when {
            game.isDraw -> "DRAW"
            else -> "${game.winner?.label?.uppercase()} WINS"
        }
        else -> "${game.currentPlayer.label.uppercase()}'S TURN"
    }
    val player = if (game.isGameOver) game.winner else game.currentPlayer
    AnimatedContent(
        targetState = text to player,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
        label = "turn"
    ) { (label, p) ->
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .border(1.dp, (p?.colors?.light ?: GoldLight).copy(alpha = 0.6f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (p != null) {
                PlayerSymbol(p, size = 16.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun BoardCenter(ui: GameUiState) {
    val game = ui.game
    val status = when {
        game.isGameOver -> "Race complete"
        ui.diceRolling -> "Rolling…"
        game.isAnimating -> "Rolled ${game.diceValue}"
        else -> "Tap Roll"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DieView(
            face = ui.diceFace,
            rolling = ui.diceRolling,
            size = 58.dp,
            accent = game.currentPlayer.colors.light
        )
        Spacer(Modifier.height(6.dp))
        Text(
            status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = GoldLight.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun PlayerPanel(
    player: Player,
    state: TreasurePlayerState,
    ui: GameUiState,
    modifier: Modifier = Modifier
) {
    val game = ui.game
    val active = !game.isGameOver && game.currentPlayer == player
    val colors = player.colors
    val score by animateIntAsState(state.score, tween(450), label = "score")
    val border by animateFloatAsState(if (active) 1f else 0f, tween(250), label = "active")
    val finished = state.lapsCompleted >= TreasureRules.LAPS_TO_FINISH
    val lap = minOf(state.lapsCompleted + 1, TreasureRules.LAPS_TO_FINISH)
    val boardSize = Board.Standard.size
    val journey = ((state.lapsCompleted * boardSize + state.position) / (TreasureRules.LAPS_TO_FINISH * boardSize.toFloat())).coerceIn(0f, 1f)

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .border(
                width = (1 + border).dp,
                color = lerpColor(MaterialTheme.colorScheme.outlineVariant, colors.light, border),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("${player.label} score ${state.score}. ")
                    append(if (finished) "Finished 2 laps." else "Lap $lap of 2.")
                    if (active) append(" Current turn.")
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerSymbol(player, size = 30.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    player.label.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (active) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "TURN",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.base)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (finished) "Laps 2 / 2 · Finished" else "Lap $lap / 2",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.85f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(journey)
                        .fillMaxHeight()
                        .background(colors.light)
                )
            }
        }
        Box(contentAlignment = Alignment.Center) {
            ScoreDelta(player, ui)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    score.toString(),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** A restrained "+5" / "-3" that floats up beside the score when it changes. */
@Composable
private fun ScoreDelta(player: Player, ui: GameUiState) {
    val effect = ui.game.lastEffect ?: return
    val popup = ui.popup ?: return
    if (effect.player != player || effect.appliedDelta == 0) return
    key(popup.id) {
        val t = remember { Animatable(0f) }
        LaunchedEffect(Unit) { t.animateTo(1f, tween(1100)) }
        val delta = effect.appliedDelta
        Text(
            if (delta > 0) "+$delta" else "$delta",
            color = if (delta > 0) GoldLight else Color(0xFFE59A84),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.graphicsLayer {
                translationX = -56.dp.toPx()
                translationY = -18.dp.toPx() * t.value
                alpha = 1f - t.value
            }
        )
    }
}

@Composable
fun ConfirmDialog(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirm.uppercase(), style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", style = MaterialTheme.typography.labelLarge) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun lerpColor(a: Color, b: Color, t: Float): Color = androidx.compose.ui.graphics.lerp(a, b, t)
