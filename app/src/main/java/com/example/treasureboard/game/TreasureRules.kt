package com.example.treasureboard.game

import kotlin.random.Random

/** Fair six-sided die. The random source is injectable for deterministic tests. */
class Dice(private val random: Random = Random.Default) {
    fun roll(): Int = random.nextInt(1, SIDES + 1)

    companion object {
        const val SIDES = 6
    }
}

/** Everything that happened during one full turn, for callers that don't animate. */
data class TurnResult(
    val state: TreasureBoardState,
    val path: List<Int>,
    val bonusPath: List<Int>,
    val effect: SpaceEffect
)

/**
 * Pure, UI-free game rules. Every function takes a state and returns a new one, so the
 * ViewModel can animate between the same primitives that [playTurn] composes for tests.
 *
 * Turn order: [beginTurn] → [stepCurrentPlayer] × roll → optional bonus steps
 * ([landedOnBonus] then [stepCurrentPlayer] × [BONUS_STEPS]) → [applyLanding] → [endTurn].
 */
class TreasureRules(val board: Board = Board.Standard) {

    fun newGame(): TreasureBoardState = TreasureBoardState()

    fun canRoll(state: TreasureBoardState): Boolean =
        state.waitingForRoll && !state.isAnimating && !state.isGameOver

    fun beginTurn(state: TreasureBoardState, roll: Int): TreasureBoardState {
        require(roll in 1..Dice.SIDES) { "Roll must be 1..${Dice.SIDES}, was $roll" }
        check(canRoll(state)) { "Cannot roll now" }
        return state.copy(
            diceValue = roll,
            turns = state.turns + 1,
            waitingForRoll = false,
            isAnimating = true,
            lastEffect = null
        )
    }

    /** Moves one space clockwise, wrapping to START and counting a lap when crossing it. */
    fun advanceOne(player: TreasurePlayerState): TreasurePlayerState {
        val next = (player.position + 1) % board.size
        val laps = if (next == 0) player.lapsCompleted + 1 else player.lapsCompleted
        return player.copy(position = next, lapsCompleted = laps)
    }

    fun stepCurrentPlayer(state: TreasureBoardState): TreasureBoardState =
        state.withPlayer(state.currentPlayer, advanceOne(state.current))

    /** True when the main roll ended on a Bonus Step, i.e. bonus movement should follow. */
    fun landedOnBonus(state: TreasureBoardState): Boolean =
        board.typeAt(state.current.position) == SpaceType.BONUS_STEP

    fun resolveSpaceEffect(player: TreasurePlayerState, type: SpaceType): TreasurePlayerState =
        when (type) {
            SpaceType.START, SpaceType.NORMAL, SpaceType.BONUS_STEP -> player
            SpaceType.COIN -> player.copy(
                score = player.score + SpaceType.COIN.points,
                coinCount = player.coinCount + 1
            )
            SpaceType.TREASURE -> player.copy(
                score = player.score + SpaceType.TREASURE.points,
                treasureCount = player.treasureCount + 1
            )
            SpaceType.GOLD_CHEST -> player.copy(
                score = player.score + SpaceType.GOLD_CHEST.points,
                goldChestCount = player.goldChestCount + 1
            )
            SpaceType.TRAP -> player.copy(
                score = maxOf(0, player.score + SpaceType.TRAP.points),
                trapCount = player.trapCount + 1
            )
        }

    /**
     * Applies exactly one landing effect for the current player's final position.
     * A Bonus Step reached through bonus movement has no further effect (no chaining).
     */
    fun applyLanding(state: TreasureBoardState): TreasureBoardState {
        val mover = state.currentPlayer
        val before = state.current
        val type = board.typeAt(before.position)
        val after = resolveSpaceEffect(before, type)
        val effect = SpaceEffect(mover, before.position, type, after.score - before.score)
        return state.withPlayer(mover, after).copy(lastEffect = effect)
    }

    /**
     * Applies the final-turn rule, then either ends the game or hands over to the other player.
     *
     * Player 1 always opens a round, so a round is complete after Player 2 moves. Once anyone
     * completes [LAPS_TO_FINISH] laps, the game ends at the end of that round: if Player 1
     * triggers it, Player 2 gets one final turn; if Player 2 triggers it, it ends immediately.
     */
    fun endTurn(state: TreasureBoardState): TreasureBoardState {
        val mover = state.currentPlayer
        val trigger = state.finishTriggeredBy
            ?: mover.takeIf { state.current.lapsCompleted >= LAPS_TO_FINISH }
        val withTrigger = state.copy(finishTriggeredBy = trigger)
        return if (trigger != null && mover == LAST_IN_ROUND) {
            finishGame(withTrigger)
        } else {
            withTrigger.copy(
                currentPlayer = mover.other,
                waitingForRoll = true,
                isAnimating = false
            )
        }
    }

    fun finishGame(state: TreasureBoardState): TreasureBoardState {
        val p1 = state.player1.score
        val p2 = state.player2.score
        return state.copy(
            winner = when {
                p1 > p2 -> Player.PLAYER_ONE
                p2 > p1 -> Player.PLAYER_TWO
                else -> null
            },
            isDraw = p1 == p2,
            isGameOver = true,
            waitingForRoll = false,
            isAnimating = false
        )
    }

    /** Plays a whole turn synchronously using the same primitives the animated flow uses. */
    fun playTurn(state: TreasureBoardState, roll: Int): TurnResult {
        var s = beginTurn(state, roll)
        val path = ArrayList<Int>(roll)
        repeat(roll) {
            s = stepCurrentPlayer(s)
            path += s.current.position
        }
        val bonusPath = ArrayList<Int>(BONUS_STEPS)
        if (landedOnBonus(s)) {
            repeat(BONUS_STEPS) {
                s = stepCurrentPlayer(s)
                bonusPath += s.current.position
            }
        }
        s = applyLanding(s)
        val effect = requireNotNull(s.lastEffect)
        return TurnResult(endTurn(s), path, bonusPath, effect)
    }

    companion object {
        const val LAPS_TO_FINISH = 2
        const val BONUS_STEPS = 2
        val LAST_IN_ROUND = Player.PLAYER_TWO
    }
}
