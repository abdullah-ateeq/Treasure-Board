package com.example.treasureboard.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.treasureboard.GameUiState
import com.example.treasureboard.game.Player
import com.example.treasureboard.game.TreasurePlayerState
import com.example.treasureboard.ui.components.GoldButton
import com.example.treasureboard.ui.components.OrnamentDivider
import com.example.treasureboard.ui.components.PanelCard
import com.example.treasureboard.ui.components.PlayerSymbol
import com.example.treasureboard.ui.components.QuietButton
import com.example.treasureboard.ui.components.ScreenBackground
import com.example.treasureboard.ui.theme.colors

@Composable
fun ResultScreen(ui: GameUiState, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    val game = ui.game
    val headline = when {
        game.isDraw -> "DRAW"
        game.winner != null -> "${game.winner.label.uppercase()} WINS"
        else -> "DRAW"
    }
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(500)) }

    ScreenBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier.graphicsLayer {
                    alpha = reveal.value
                    translationY = (1f - reveal.value) * 24.dp.toPx()
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (game.winner != null) PlayerSymbol(game.winner, size = 44.dp)
                else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlayerSymbol(Player.PLAYER_ONE, size = 36.dp)
                    PlayerSymbol(Player.PLAYER_TWO, size = 36.dp)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    headline,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(10.dp))
                OrnamentDivider()
            }
            Spacer(Modifier.height(24.dp))

            Column(Modifier.widthIn(max = 460.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "FINAL SCORES",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScoreCard(Player.PLAYER_ONE, game.player1.score, game.winner == Player.PLAYER_ONE, Modifier.weight(1f))
                    ScoreCard(Player.PLAYER_TWO, game.player2.score, game.winner == Player.PLAYER_TWO, Modifier.weight(1f))
                }

                PanelCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.weight(1f))
                        PlayerSymbol(Player.PLAYER_ONE, Modifier.width(56.dp), size = 16.dp)
                        PlayerSymbol(Player.PLAYER_TWO, Modifier.width(56.dp), size = 16.dp)
                    }
                    Spacer(Modifier.height(6.dp))
                    StatRow("Treasure Collected", game.player1, game.player2) { it.treasureCount }
                    StatRow("Gold Chests Landed", game.player1, game.player2) { it.goldChestCount }
                    StatRow("Coins Collected", game.player1, game.player2) { it.coinCount }
                    StatRow("Traps Hit", game.player1, game.player2) { it.trapCount }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SummaryRow("Total Turns", game.turns.toString())
                    SummaryRow("Match Duration", formatDuration(ui.durationMillis))
                }

                Spacer(Modifier.height(4.dp))
                GoldButton("Play Again", onPlayAgain, Modifier.fillMaxWidth())
                QuietButton("Home", onHome, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ScoreCard(player: Player, score: Int, winner: Boolean, modifier: Modifier) {
    PanelCard(
        modifier.semantics(mergeDescendants = true) { contentDescription = "${player.label} final score $score" },
        borderColor = if (winner) player.colors.light else MaterialTheme.colorScheme.outlineVariant
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerSymbol(player, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text(player.label.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(8.dp))
        Text(score.toString(), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        if (winner) {
            Text("WINNER", style = MaterialTheme.typography.labelSmall, color = player.colors.light)
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    p1: TreasurePlayerState,
    p2: TreasurePlayerState,
    value: (TreasurePlayerState) -> Int
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: Player 1 ${value(p1)}, Player 2 ${value(p2)}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(value(p1).toString(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.width(56.dp), color = MaterialTheme.colorScheme.onSurface)
        Text(value(p2).toString(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.width(56.dp), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
