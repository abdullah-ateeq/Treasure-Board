package com.example.treasureboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasureboard.data.AppSettings
import com.example.treasureboard.data.SettingsRepository
import com.example.treasureboard.game.Dice
import com.example.treasureboard.game.Player
import com.example.treasureboard.game.SpaceEffect
import com.example.treasureboard.game.SpaceType
import com.example.treasureboard.game.TreasureBoardState
import com.example.treasureboard.game.TreasureRules
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/** A short floating score label shown near the landing space. */
data class ScorePopup(
    val id: Long,
    val player: Player,
    val spaceId: Int,
    val text: String,
    val positive: Boolean
)

data class GameUiState(
    val game: TreasureBoardState = TreasureBoardState(),
    val diceFace: Int = 1,
    val diceRolling: Boolean = false,
    val popup: ScorePopup? = null,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val endedAtMillis: Long? = null
) {
    val durationMillis: Long get() = (endedAtMillis ?: System.currentTimeMillis()) - startedAtMillis
}

sealed interface GameEvent {
    data object DiceRolled : GameEvent
    data object TokenStep : GameEvent
    data object BonusMove : GameEvent
    data class Landed(val effect: SpaceEffect) : GameEvent
    data class GameOver(val winner: Player?) : GameEvent
}

class GameViewModel(
    private val repository: SettingsRepository,
    private val rules: TreasureRules = TreasureRules(),
    private val dice: Dice = Dice()
) : ViewModel() {

    val board get() = rules.board

    private val _ui = MutableStateFlow(GameUiState())
    val ui: StateFlow<GameUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private var turnJob: Job? = null
    private var popupCounter = 0L
    private val faceRandom = Random.Default

    val canRoll: Boolean
        get() = rules.canRoll(_ui.value.game) && turnJob?.isActive != true

    fun newGame() {
        turnJob?.cancel()
        turnJob = null
        _ui.value = GameUiState(game = rules.newGame())
    }

    fun rollDice() {
        if (!canRoll) return
        val roll = dice.roll()
        _ui.update { it.copy(game = rules.beginTurn(it.game, roll), diceRolling = true, popup = null) }
        turnJob = viewModelScope.launch { runTurn(roll) }
    }

    private suspend fun runTurn(roll: Int) {
        // 1. Dice animation (~315ms of changing faces, landing on the real value).
        _events.tryEmit(GameEvent.DiceRolled)
        var face = _ui.value.diceFace
        repeat(DICE_FRAMES) {
            face = (1..6).filter { it != face }.random(faceRandom)
            _ui.update { it.copy(diceFace = face) }
            delay(DICE_FRAME_MS)
        }
        _ui.update { it.copy(diceFace = roll, diceRolling = false) }
        delay(POST_ROLL_PAUSE_MS)

        // 2. Space-by-space movement.
        val stepMs = if (settings.value.fastMovement) FAST_STEP_MS else STEP_MS
        repeat(roll) { step(stepMs) }

        // 3. Optional single bonus movement (never chained).
        if (rules.landedOnBonus(_ui.value.game)) {
            val game = _ui.value.game
            showPopup(game.currentPlayer, game.current.position, "+2 BONUS STEPS", positive = true)
            _events.tryEmit(GameEvent.BonusMove)
            delay(BONUS_PAUSE_MS)
            repeat(TreasureRules.BONUS_STEPS) { step(stepMs) }
        }

        // 4. Exactly one landing effect.
        _ui.update { it.copy(game = rules.applyLanding(it.game)) }
        val effect = requireNotNull(_ui.value.game.lastEffect)
        popupText(effect.type)?.let { showPopup(effect.player, effect.spaceId, it, effect.type != SpaceType.TRAP) }
        _events.tryEmit(GameEvent.Landed(effect))
        delay(if (popupText(effect.type) != null) EFFECT_PAUSE_MS else QUIET_PAUSE_MS)

        // 5. Final-turn rule, then next player or game over.
        _ui.update { it.copy(game = rules.endTurn(it.game)) }
        val ended = _ui.value.game
        if (ended.isGameOver) {
            _ui.update { it.copy(endedAtMillis = System.currentTimeMillis()) }
            repository.recordGame(ended.winner, ended.player1.score, ended.player2.score)
            _events.tryEmit(GameEvent.GameOver(ended.winner))
        }
    }

    private suspend fun step(stepMs: Long) {
        _ui.update { it.copy(game = rules.stepCurrentPlayer(it.game)) }
        _events.tryEmit(GameEvent.TokenStep)
        delay(stepMs)
    }

    private fun showPopup(player: Player, spaceId: Int, text: String, positive: Boolean) {
        popupCounter++
        _ui.update { it.copy(popup = ScorePopup(popupCounter, player, spaceId, text, positive)) }
    }

    private fun popupText(type: SpaceType): String? = when (type) {
        SpaceType.COIN -> "+2 COINS"
        SpaceType.TREASURE -> "+5 TREASURE"
        SpaceType.GOLD_CHEST -> "+8 GOLD"
        SpaceType.TRAP -> "-3 TRAP"
        SpaceType.START, SpaceType.NORMAL, SpaceType.BONUS_STEP -> null
    }

    companion object {
        private const val DICE_FRAMES = 7
        private const val DICE_FRAME_MS = 45L
        private const val POST_ROLL_PAUSE_MS = 140L
        const val STEP_MS = 100L
        const val FAST_STEP_MS = 60L
        private const val BONUS_PAUSE_MS = 280L
        private const val EFFECT_PAUSE_MS = 420L
        private const val QUIET_PAUSE_MS = 120L
    }
}
