package com.example.treasureboard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.treasureboard.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class BoardTheme { CLASSIC, MIDNIGHT }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val fastMovement: Boolean = false,
    val showSpaceLabels: Boolean = true
)

data class GameStats(
    val gamesPlayed: Int = 0,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val draws: Int = 0,
    val highestScore: Int = 0
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "treasure_board")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val BOARD = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val FAST = booleanPreferencesKey("fast_movement")
        val LABELS = booleanPreferencesKey("space_labels")
        val GAMES = intPreferencesKey("games_played")
        val P1_WINS = intPreferencesKey("p1_wins")
        val P2_WINS = intPreferencesKey("p2_wins")
        val DRAWS = intPreferencesKey("draws")
        val HIGH = intPreferencesKey("highest_score")
    }

    private val preferences: Flow<Preferences> = context.dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val settings: Flow<AppSettings> = preferences.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME].toEnum(ThemeMode.SYSTEM),
            boardTheme = p[Keys.BOARD].toEnum(BoardTheme.CLASSIC),
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            fastMovement = p[Keys.FAST] ?: false,
            showSpaceLabels = p[Keys.LABELS] ?: true
        )
    }

    val stats: Flow<GameStats> = preferences.map { p ->
        GameStats(
            gamesPlayed = p[Keys.GAMES] ?: 0,
            player1Wins = p[Keys.P1_WINS] ?: 0,
            player2Wins = p[Keys.P2_WINS] ?: 0,
            draws = p[Keys.DRAWS] ?: 0,
            highestScore = p[Keys.HIGH] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = context.dataStore.edit { it[Keys.BOARD] = theme.name }
    suspend fun setSound(enabled: Boolean) = context.dataStore.edit { it[Keys.SOUND] = enabled }
    suspend fun setHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setFastMovement(enabled: Boolean) = context.dataStore.edit { it[Keys.FAST] = enabled }
    suspend fun setShowSpaceLabels(enabled: Boolean) = context.dataStore.edit { it[Keys.LABELS] = enabled }

    suspend fun recordGame(winner: Player?, player1Score: Int, player2Score: Int) {
        context.dataStore.edit { p ->
            p[Keys.GAMES] = (p[Keys.GAMES] ?: 0) + 1
            when (winner) {
                Player.PLAYER_ONE -> p[Keys.P1_WINS] = (p[Keys.P1_WINS] ?: 0) + 1
                Player.PLAYER_TWO -> p[Keys.P2_WINS] = (p[Keys.P2_WINS] ?: 0) + 1
                null -> p[Keys.DRAWS] = (p[Keys.DRAWS] ?: 0) + 1
            }
            p[Keys.HIGH] = maxOf(p[Keys.HIGH] ?: 0, player1Score, player2Score)
        }
    }

    suspend fun resetStats() {
        context.dataStore.edit { p ->
            listOf(Keys.GAMES, Keys.P1_WINS, Keys.P2_WINS, Keys.DRAWS, Keys.HIGH).forEach { p.remove(it) }
        }
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
