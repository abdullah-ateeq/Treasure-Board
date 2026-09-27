package com.example.treasureboard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.treasureboard.data.BoardTheme
import com.example.treasureboard.game.Player
import com.example.treasureboard.game.SpaceType
import com.example.treasureboard.game.description
import com.example.treasureboard.ui.board.drawTile
import com.example.treasureboard.ui.board.drawToken
import com.example.treasureboard.ui.components.PanelCard
import com.example.treasureboard.ui.components.ScreenBackground
import com.example.treasureboard.ui.components.ScreenHeader
import com.example.treasureboard.ui.theme.GoldLight
import com.example.treasureboard.ui.theme.palette

private val RULES = listOf(
    "Roll the die" to "Players take turns rolling one six-sided die. Player 1 always starts.",
    "Move around the board" to "Your token moves clockwise exactly the number rolled, one space at a time.",
    "Collect treasure" to "Land on Coin, Treasure or Gold Chest spaces to gain points — every time you land.",
    "Beware of traps" to "Traps take away 3 points. Your score never drops below zero.",
    "Take the bonus" to "Bonus spaces move you 2 more spaces. Only the final space counts, and bonuses never chain.",
    "Complete 2 laps" to "When a player completes 2 laps, the round is finished so both players get equal turns.",
    "Highest score wins" to "Compare scores at the end. Equal scores are a draw."
)

private val EXAMPLE = listOf(SpaceType.NORMAL, SpaceType.COIN, SpaceType.TRAP, SpaceType.NORMAL, SpaceType.TREASURE)

@Composable
fun HowToPlayScreen(boardTheme: BoardTheme, onBack: () -> Unit) {
    val palette = boardTheme.palette
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("How to Play", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PanelCard(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                    Text("EXAMPLE TURN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(EXAMPLE.size.toFloat())
                            .semantics {
                                contentDescription = "Example: Player 1 rolls 4, moves four spaces and lands on a treasure space for plus 5 points"
                            }
                    ) {
                        val cell = size.width / EXAMPLE.size
                        val y = size.height / 2
                        EXAMPLE.forEachIndexed { i, type ->
                            drawTile(type, Offset(cell * (i + 0.5f), y), cell, palette, Offset(1f, 0f), label = null)
                        }
                        drawLine(
                            GoldLight.copy(alpha = 0.8f),
                            Offset(cell * 0.5f, y - cell * 0.47f),
                            Offset(cell * 4.5f, y - cell * 0.47f),
                            strokeWidth = cell * 0.035f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(cell * 0.08f, cell * 0.08f))
                        )
                        drawToken(Player.PLAYER_ONE, Offset(cell * 4.72f, y + cell * 0.2f), cell * 0.19f)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Player 1 rolls a 4, passes the coin and trap, and lands on Treasure: +5 points. Only the landing space counts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PanelCard(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                    RULES.forEachIndexed { i, (title, body) ->
                        Row(Modifier.padding(vertical = 6.dp)) {
                            Text(
                                "${i + 1}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(28.dp)
                            )
                            Column {
                                Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(2.dp))
                                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                PanelCard(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                    Text("BOARD SPACES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    SpaceType.entries.forEach { type ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .semantics(mergeDescendants = true) { contentDescription = type.description },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(Modifier.size(40.dp)) {
                                drawTile(type, center, size.minDimension, palette, Offset(1f, 0f), label = null)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(type.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(type.effect, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private val SpaceType.title: String
    get() = when (this) {
        SpaceType.START -> "START"
        SpaceType.NORMAL -> "NORMAL"
        SpaceType.COIN -> "COIN"
        SpaceType.TREASURE -> "TREASURE"
        SpaceType.GOLD_CHEST -> "GOLD CHEST"
        SpaceType.TRAP -> "TRAP"
        SpaceType.BONUS_STEP -> "BONUS STEP"
    }

private val SpaceType.effect: String
    get() = when (this) {
        SpaceType.START -> "Crossing it completes a lap"
        SpaceType.NORMAL -> "No effect"
        SpaceType.COIN -> "+2 points"
        SpaceType.TREASURE -> "+5 points"
        SpaceType.GOLD_CHEST -> "+8 points"
        SpaceType.TRAP -> "−3 points, never below 0"
        SpaceType.BONUS_STEP -> "Move 2 more spaces"
    }
