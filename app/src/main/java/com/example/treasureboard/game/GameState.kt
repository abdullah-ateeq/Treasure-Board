package com.example.treasureboard.game

enum class Player(val number: Int) {
    PLAYER_ONE(1),
    PLAYER_TWO(2);

    val other: Player get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE
    val label: String get() = "Player $number"
}

data class TreasurePlayerState(
    val position: Int = 0,
    val score: Int = 0,
    val lapsCompleted: Int = 0,
    val coinCount: Int = 0,
    val treasureCount: Int = 0,
    val goldChestCount: Int = 0,
    val trapCount: Int = 0
)

/** The single landing effect applied at the end of a move. */
data class SpaceEffect(
    val player: Player,
    val spaceId: Int,
    val type: SpaceType,
    /** Score change actually applied (a trap may remove less than 3 near zero). */
    val appliedDelta: Int
)

data class TreasureBoardState(
    val player1: TreasurePlayerState = TreasurePlayerState(),
    val player2: TreasurePlayerState = TreasurePlayerState(),
    val currentPlayer: Player = Player.PLAYER_ONE,
    val diceValue: Int? = null,
    val waitingForRoll: Boolean = true,
    val finishTriggeredBy: Player? = null,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val turns: Int = 0,
    val isAnimating: Boolean = false,
    val isGameOver: Boolean = false,
    val lastEffect: SpaceEffect? = null
) {
    fun player(player: Player): TreasurePlayerState =
        if (player == Player.PLAYER_ONE) player1 else player2

    fun withPlayer(player: Player, state: TreasurePlayerState): TreasureBoardState =
        if (player == Player.PLAYER_ONE) copy(player1 = state) else copy(player2 = state)

    val current: TreasurePlayerState get() = player(currentPlayer)
}
