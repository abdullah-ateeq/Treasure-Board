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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.treasureboard.game.Player
import com.example.treasureboard.ui.components.OrnamentDivider
import com.example.treasureboard.ui.components.PanelCard
import com.example.treasureboard.ui.components.PlayerSymbol
import com.example.treasureboard.ui.components.ScreenBackground
import com.example.treasureboard.ui.components.ScreenHeader

private val FEATURES = listOf(
    "Local 2 Player",
    "Dice Movement",
    "Treasure Scoring",
    "2-Lap Matches",
    "Offline Play",
    "Premium Board Themes"
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("About", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlayerSymbol(Player.PLAYER_ONE, size = 26.dp)
                    PlayerSymbol(Player.PLAYER_TWO, size = 26.dp)
                }
                Text("Treasure Board", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                Text("Version 1.0", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OrnamentDivider()
                Text(
                    "A polished local two-player board race where players roll, explore, collect treasure points, " +
                        "and compete for the highest score.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 480.dp)
                )
                PanelCard(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
                    FEATURES.forEach { feature ->
                        Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            val gold = MaterialTheme.colorScheme.primary
                            Canvas(Modifier.size(10.dp)) {
                                val c = center
                                val r = size.minDimension / 2
                                drawPath(Path().apply {
                                    moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close()
                                }, gold)
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(feature, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
