package com.example.treasureboard.game

enum class SpaceType {
    START,
    NORMAL,
    COIN,
    TREASURE,
    GOLD_CHEST,
    TRAP,
    BONUS_STEP
}

data class BoardSpace(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: SpaceType
)

/**
 * A fixed looping track. Spaces are ordered clockwise; index 0 is always START.
 * Coordinates are normalized (0f..1f) so the board scales to any canvas size.
 */
class Board(val spaces: List<BoardSpace>) {

    init {
        require(spaces.isNotEmpty()) { "Board needs at least one space" }
        require(spaces.first().type == SpaceType.START) { "Space 0 must be START" }
    }

    val size: Int get() = spaces.size

    operator fun get(index: Int): BoardSpace = spaces[index]

    fun typeAt(index: Int): SpaceType = spaces[index].type

    companion object {
        /** Cells per side of the square grid the track is laid on. */
        const val GRID = 8

        /**
         * Handcrafted layout: START + 9 Normal, 6 Coin, 4 Treasure, 2 Gold Chest, 4 Trap, 2 Bonus Step.
         */
        private val STANDARD_LAYOUT = listOf(
            SpaceType.START,      // 0  bottom-left corner
            SpaceType.NORMAL,     // 1  left column, moving up
            SpaceType.COIN,       // 2
            SpaceType.NORMAL,     // 3
            SpaceType.TRAP,       // 4
            SpaceType.COIN,       // 5
            SpaceType.NORMAL,     // 6
            SpaceType.TREASURE,   // 7  top-left corner
            SpaceType.NORMAL,     // 8  top row, moving right
            SpaceType.BONUS_STEP, // 9
            SpaceType.COIN,       // 10
            SpaceType.TRAP,       // 11
            SpaceType.NORMAL,     // 12
            SpaceType.TREASURE,   // 13
            SpaceType.GOLD_CHEST, // 14 top-right corner
            SpaceType.NORMAL,     // 15 right column, moving down
            SpaceType.COIN,       // 16
            SpaceType.TRAP,       // 17
            SpaceType.NORMAL,     // 18
            SpaceType.BONUS_STEP, // 19
            SpaceType.COIN,       // 20
            SpaceType.TREASURE,   // 21 bottom-right corner
            SpaceType.NORMAL,     // 22 bottom row, moving left
            SpaceType.COIN,       // 23
            SpaceType.TRAP,       // 24
            SpaceType.NORMAL,     // 25
            SpaceType.GOLD_CHEST, // 26
            SpaceType.TREASURE    // 27
        )

        val Standard: Board by lazy { perimeterBoard(STANDARD_LAYOUT) }

        /**
         * Places [types] clockwise around the perimeter of a square grid, starting at the
         * bottom-left corner and heading up the left side.
         */
        fun perimeterBoard(types: List<SpaceType>): Board {
            val cells = perimeterCells(GRID)
            require(types.size == cells.size) { "Layout must have ${cells.size} spaces" }
            return Board(
                types.mapIndexed { index, type ->
                    val (col, row) = cells[index]
                    BoardSpace(
                        id = index,
                        x = (col + 0.5f) / GRID,
                        y = (row + 0.5f) / GRID,
                        type = type
                    )
                }
            )
        }

        /** Builds a board from arbitrary types with placeholder coordinates (useful for tests). */
        fun fromTypes(types: List<SpaceType>): Board = Board(
            types.mapIndexed { index, type ->
                BoardSpace(index, index / types.size.toFloat(), 0f, type)
            }
        )

        private fun perimeterCells(n: Int): List<Pair<Int, Int>> {
            val last = n - 1
            val cells = mutableListOf<Pair<Int, Int>>()
            for (row in last downTo 1) cells += 0 to row          // left, upward
            for (col in 0 until last) cells += col to 0           // top, rightward
            for (row in 0 until last) cells += last to row        // right, downward
            for (col in last downTo 1) cells += col to last       // bottom, leftward
            return cells
        }
    }
}

val SpaceType.points: Int
    get() = when (this) {
        SpaceType.COIN -> 2
        SpaceType.TREASURE -> 5
        SpaceType.GOLD_CHEST -> 8
        SpaceType.TRAP -> -3
        SpaceType.START, SpaceType.NORMAL, SpaceType.BONUS_STEP -> 0
    }

/** Accessible description, e.g. "Treasure space, plus 5 points". */
val SpaceType.description: String
    get() = when (this) {
        SpaceType.START -> "Start space"
        SpaceType.NORMAL -> "Normal space, no effect"
        SpaceType.COIN -> "Coin space, plus 2 points"
        SpaceType.TREASURE -> "Treasure space, plus 5 points"
        SpaceType.GOLD_CHEST -> "Gold chest space, plus 8 points"
        SpaceType.TRAP -> "Trap space, minus 3 points"
        SpaceType.BONUS_STEP -> "Bonus step space, move 2 more spaces"
    }
