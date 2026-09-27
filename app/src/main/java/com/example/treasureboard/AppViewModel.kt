package com.example.treasureboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasureboard.data.AppSettings
import com.example.treasureboard.data.BoardTheme
import com.example.treasureboard.data.GameStats
import com.example.treasureboard.data.SettingsRepository
import com.example.treasureboard.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Settings and statistics shared by Home, Settings and the app theme. */
class AppViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val stats: StateFlow<GameStats> =
        repository.stats.stateIn(viewModelScope, SharingStarted.Eagerly, GameStats())

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = launch { repository.setBoardTheme(theme) }
    fun setSound(enabled: Boolean) = launch { repository.setSound(enabled) }
    fun setHaptics(enabled: Boolean) = launch { repository.setHaptics(enabled) }
    fun setFastMovement(enabled: Boolean) = launch { repository.setFastMovement(enabled) }
    fun setShowSpaceLabels(enabled: Boolean) = launch { repository.setShowSpaceLabels(enabled) }
    fun resetStats() = launch { repository.resetStats() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
