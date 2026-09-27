package com.example.treasureboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.treasureboard.data.AppSettings
import com.example.treasureboard.data.BoardTheme
import com.example.treasureboard.data.GameStats
import com.example.treasureboard.data.ThemeMode
import com.example.treasureboard.ui.components.PanelCard
import com.example.treasureboard.ui.components.QuietButton
import com.example.treasureboard.ui.components.ScreenBackground
import com.example.treasureboard.ui.components.ScreenHeader
import com.example.treasureboard.ui.components.SegmentedChoice
import com.example.treasureboard.ui.theme.Gold

@Composable
fun SettingsScreen(
    settings: AppSettings,
    stats: GameStats,
    onThemeMode: (ThemeMode) -> Unit,
    onBoardTheme: (BoardTheme) -> Unit,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onFastMovement: (Boolean) -> Unit,
    onShowLabels: (Boolean) -> Unit,
    onResetStats: () -> Unit,
    onBack: () -> Unit
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("Settings", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val width = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                PanelCard(width) {
                    SectionLabel("Theme")
                    SegmentedChoice(
                        options = listOf(ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark", ThemeMode.SYSTEM to "System"),
                        selected = settings.themeMode,
                        onSelect = onThemeMode
                    )
                    Spacer(Modifier.height(16.dp))
                    SectionLabel("Board Theme")
                    SegmentedChoice(
                        options = listOf(BoardTheme.CLASSIC to "Classic", BoardTheme.MIDNIGHT to "Midnight"),
                        selected = settings.boardTheme,
                        onSelect = onBoardTheme
                    )
                }

                PanelCard(width) {
                    ToggleRow("Sound Effects", "Dice, movement and treasure cues", settings.soundEnabled, onSound)
                    Divider()
                    ToggleRow("Haptic Feedback", "Subtle vibration on key moments", settings.hapticsEnabled, onHaptics)
                    Divider()
                    ToggleRow("Fast Movement", "Quicker token steps", settings.fastMovement, onFastMovement)
                    Divider()
                    ToggleRow("Show Space Labels", "Point values printed on spaces", settings.showSpaceLabels, onShowLabels)
                }

                PanelCard(width) {
                    SectionLabel("Statistics")
                    Text(
                        "${stats.gamesPlayed} games · P1 ${stats.player1Wins} wins · P2 ${stats.player2Wins} wins · " +
                            "${stats.draws} draws · best ${stats.highestScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    QuietButton("Reset Statistics", { confirmReset = true }, Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Reset statistics?",
            body = "Games played, wins, draws and highest score will be cleared.",
            confirm = "Reset",
            onConfirm = { confirmReset = false; onResetStats() },
            onDismiss = { confirmReset = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun Divider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = Gold, checkedThumbColor = MaterialTheme.colorScheme.surface)
        )
    }
}
