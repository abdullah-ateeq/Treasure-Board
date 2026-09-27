package com.example.treasureboard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.treasureboard.data.GameStats
import com.example.treasureboard.game.Player
import com.example.treasureboard.ui.components.GoldButton
import com.example.treasureboard.ui.components.OrnamentDivider
import com.example.treasureboard.ui.components.PanelCard
import com.example.treasureboard.ui.components.PlayerSymbol
import com.example.treasureboard.ui.components.QuietButton
import com.example.treasureboard.ui.components.ScreenBackground
import com.example.treasureboard.ui.components.StatTile

@Composable
fun HomeScreen(
    stats: GameStats,
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    ScreenBackground {
        val motif = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        Canvas(Modifier.fillMaxSize()) {
            // Faint board-loop motif behind the menu: two hairline rounded squares.
            val s = size.minDimension * 0.92f
            val topLeft = Offset((size.width - s) / 2, (size.height - s) / 2)
            drawRoundRect(motif, topLeft, Size(s, s), CornerRadius(s * 0.1f), style = Stroke(width = 1.5f))
            val inset = s * 0.12f
            drawRoundRect(
                motif, topLeft + Offset(inset, inset), Size(s - inset * 2, s - inset * 2),
                CornerRadius(s * 0.06f), style = Stroke(width = 1f)
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlayerSymbol(Player.PLAYER_ONE, size = 22.dp)
                PlayerSymbol(Player.PLAYER_TWO, size = 22.dp)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "TREASURE\nBOARD",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            OrnamentDivider()
            Spacer(Modifier.height(14.dp))
            Text(
                "Roll. Explore. Collect the most treasure.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            Column(
                Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoldButton("Play", onPlay, Modifier.fillMaxWidth())
                QuietButton("How to Play", onHowToPlay, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuietButton("Settings", onSettings, Modifier.weight(1f))
                    QuietButton("About", onAbout, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(32.dp))
            PanelCard(
                Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Statistics: ${stats.gamesPlayed} games played, " +
                            "Player 1 ${stats.player1Wins} wins, Player 2 ${stats.player2Wins} wins, " +
                            "highest score ${stats.highestScore}"
                    }
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatTile(stats.gamesPlayed.toString(), "Games", Modifier.weight(1f))
                    StatTile(stats.player1Wins.toString(), "P1 Wins", Modifier.weight(1f))
                    StatTile(stats.player2Wins.toString(), "P2 Wins", Modifier.weight(1f))
                    StatTile(stats.highestScore.toString(), "Best", Modifier.weight(1f))
                }
            }
        }
    }
}
