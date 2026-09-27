package com.example.treasureboard.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TreasureRulesTest {

    private val rules = TreasureRules()

    private fun stateWith(
        p1: TreasurePlayerState = TreasurePlayerState(),
        p2: TreasurePlayerState = TreasurePlayerState(),
        current: Player = Player.PLAYER_ONE
    ) = TreasureBoardState(player1 = p1, player2 = p2, currentPlayer = current)

    @Test
    fun standardBoardHasFixedDistribution() {
        val counts = Board.Standard.spaces.groupingBy { it.type }.eachCount()
        assertEquals(28, Board.Standard.size)
        assertEquals(1, counts[SpaceType.START])
        assertEquals(9, counts[SpaceType.NORMAL])
        assertEquals(6, counts[SpaceType.COIN])
        assertEquals(4, counts[SpaceType.TREASURE])
        assertEquals(2, counts[SpaceType.GOLD_CHEST])
        assertEquals(4, counts[SpaceType.TRAP])
        assertEquals(2, counts[SpaceType.BONUS_STEP])
        assertTrue(Board.Standard.spaces.all { it.x in 0f..1f && it.y in 0f..1f })
    }

    @Test
    fun diceValuesStayWithinOneToSixAndCoverAllFaces() {
        val dice = Dice(Random(42))
        val rolls = List(6_000) { dice.roll() }
        assertTrue(rolls.all { it in 1..6 })
        assertEquals((1..6).toSet(), rolls.toSet())
    }

    @Test
    fun normalMovementAdvancesExactlyRolledSpaces() {
        val result = rules.playTurn(rules.newGame(), 3)
        assertEquals(listOf(1, 2, 3), result.path)
        assertEquals(3, result.state.player1.position)
        assertEquals(0, result.state.player1.score) // space 3 is NORMAL
        assertEquals(Player.PLAYER_TWO, result.state.currentPlayer)
        assertEquals(1, result.state.turns)
    }

    @Test
    fun movementWrapsPastStartAndCountsOneLap() {
        val start = stateWith(p1 = TreasurePlayerState(position = 25))
        val result = rules.playTurn(start, 4)
        assertEquals(listOf(26, 27, 0, 1), result.path)
        assertEquals(1, result.state.player1.position)
        assertEquals(1, result.state.player1.lapsCompleted)
    }

    @Test
    fun startingOnStartDoesNotCountALap() {
        val result = rules.playTurn(rules.newGame(), 6)
        assertEquals(0, result.state.player1.lapsCompleted)
    }

    @Test
    fun landingExactlyOnStartCountsALap() {
        val start = stateWith(p1 = TreasurePlayerState(position = 22))
        val result = rules.playTurn(start, 6)
        assertEquals(0, result.state.player1.position)
        assertEquals(1, result.state.player1.lapsCompleted)
    }

    @Test
    fun coinAddsTwo() {
        val result = rules.playTurn(rules.newGame(), 2) // space 2 is COIN
        assertEquals(2, result.state.player1.score)
        assertEquals(1, result.state.player1.coinCount)
        assertEquals(SpaceType.COIN, result.effect.type)
    }

    @Test
    fun treasureAddsFive() {
        val start = stateWith(p1 = TreasurePlayerState(position = 1))
        val result = rules.playTurn(start, 6) // space 7 is TREASURE
        assertEquals(5, result.state.player1.score)
        assertEquals(1, result.state.player1.treasureCount)
    }

    @Test
    fun goldChestAddsEight() {
        val start = stateWith(p1 = TreasurePlayerState(position = 10, score = 1))
        val result = rules.playTurn(start, 4) // space 14 is GOLD_CHEST
        assertEquals(9, result.state.player1.score)
        assertEquals(1, result.state.player1.goldChestCount)
    }

    @Test
    fun trapSubtractsThreeButNeverBelowZero() {
        val trapped = rules.playTurn(stateWith(p1 = TreasurePlayerState(score = 10)), 4) // space 4 TRAP
        assertEquals(7, trapped.state.player1.score)

        val nearZero = rules.playTurn(stateWith(p1 = TreasurePlayerState(score = 1)), 4)
        assertEquals(0, nearZero.state.player1.score)
        assertEquals(-1, nearZero.effect.appliedDelta)
        assertEquals(1, nearZero.state.player1.trapCount)
    }

    @Test
    fun bonusStepMovesTwoExtraSpaces() {
        val start = stateWith(p1 = TreasurePlayerState(position = 5))
        val result = rules.playTurn(start, 4) // 9 is BONUS_STEP -> 11
        assertEquals(listOf(6, 7, 8, 9), result.path)
        assertEquals(listOf(10, 11), result.bonusPath)
        assertEquals(11, result.state.player1.position)
    }

    @Test
    fun bonusDestinationIsScoredOnce() {
        val start = stateWith(p1 = TreasurePlayerState(position = 15, score = 4))
        val result = rules.playTurn(start, 4) // 19 BONUS -> 21 TREASURE
        assertEquals(21, result.state.player1.position)
        assertEquals(9, result.state.player1.score)
        assertEquals(1, result.state.player1.treasureCount)
        assertEquals(0, result.state.player1.coinCount) // passed over COIN on 20, not scored
    }

    @Test
    fun bonusDoesNotChainIntoAnotherBonus() {
        val types = MutableList(12) { SpaceType.NORMAL }.apply {
            this[0] = SpaceType.START
            this[2] = SpaceType.BONUS_STEP
            this[4] = SpaceType.BONUS_STEP
        }
        val chainRules = TreasureRules(Board.fromTypes(types))
        val result = chainRules.playTurn(chainRules.newGame(), 2)
        assertEquals(listOf(3, 4), result.bonusPath)
        assertEquals(4, result.state.player1.position)
        assertEquals(SpaceType.BONUS_STEP, result.effect.type)
        assertEquals(0, result.effect.appliedDelta)
    }

    @Test
    fun bonusMovementCanCrossStartAndCountLap() {
        val types = MutableList(10) { SpaceType.NORMAL }.apply {
            this[0] = SpaceType.START
            this[9] = SpaceType.BONUS_STEP
        }
        val wrapRules = TreasureRules(Board.fromTypes(types))
        val result = wrapRules.playTurn(stateWith(p1 = TreasurePlayerState(position = 6)), 3)
        assertEquals(listOf(0, 1), result.bonusPath)
        assertEquals(1, result.state.player1.lapsCompleted)
    }

    @Test
    fun playerOneFinishingSecondLapGivesPlayerTwoAFinalTurn() {
        val start = stateWith(p1 = TreasurePlayerState(position = 26, lapsCompleted = 1, score = 20))
        val afterP1 = rules.playTurn(start, 3).state
        assertEquals(2, afterP1.player1.lapsCompleted)
        assertEquals(Player.PLAYER_ONE, afterP1.finishTriggeredBy)
        assertFalse(afterP1.isGameOver)
        assertEquals(Player.PLAYER_TWO, afterP1.currentPlayer)
        assertTrue(rules.canRoll(afterP1))

        val afterP2 = rules.playTurn(afterP1, 1).state
        assertTrue(afterP2.isGameOver)
        assertFalse(rules.canRoll(afterP2))
    }

    @Test
    fun playerTwoFinishingSecondLapEndsImmediately() {
        val start = stateWith(
            p2 = TreasurePlayerState(position = 27, lapsCompleted = 1),
            current = Player.PLAYER_TWO
        )
        val end = rules.playTurn(start, 2).state
        assertEquals(Player.PLAYER_TWO, end.finishTriggeredBy)
        assertTrue(end.isGameOver)
    }

    @Test
    fun oneLapDoesNotTriggerFinish() {
        val start = stateWith(p1 = TreasurePlayerState(position = 26))
        val end = rules.playTurn(start, 3).state
        assertNull(end.finishTriggeredBy)
        assertFalse(end.isGameOver)
    }

    @Test
    fun higherScoreWins() {
        val end = rules.finishGame(
            stateWith(p1 = TreasurePlayerState(score = 46), p2 = TreasurePlayerState(score = 39))
        )
        assertEquals(Player.PLAYER_ONE, end.winner)
        assertFalse(end.isDraw)

        val p2Wins = rules.finishGame(
            stateWith(p1 = TreasurePlayerState(score = 12), p2 = TreasurePlayerState(score = 30))
        )
        assertEquals(Player.PLAYER_TWO, p2Wins.winner)
    }

    @Test
    fun equalScoresAreADraw() {
        val end = rules.finishGame(
            stateWith(p1 = TreasurePlayerState(score = 25), p2 = TreasurePlayerState(score = 25))
        )
        assertNull(end.winner)
        assertTrue(end.isDraw)
        assertTrue(end.isGameOver)
    }

    @Test(expected = IllegalStateException::class)
    fun cannotRollWhileAnimating() {
        val moving = rules.beginTurn(rules.newGame(), 3)
        rules.beginTurn(moving, 2)
    }

    @Test
    fun fullSimulatedMatchAlwaysEndsWithEqualTurns() {
        val dice = Dice(Random(7))
        repeat(200) {
            var state = rules.newGame()
            while (!state.isGameOver) state = rules.playTurn(state, dice.roll()).state
            assertEquals(0, state.turns % 2)
            assertTrue(state.player1.lapsCompleted >= 2 || state.player2.lapsCompleted >= 2)
            assertTrue(state.player1.score >= 0 && state.player2.score >= 0)
        }
    }
}
