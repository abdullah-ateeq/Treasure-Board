package com.example.treasureboard.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.treasureboard.data.BoardTheme
import com.example.treasureboard.game.Player

// Antique gold
val Gold = Color(0xFFC9A45C)
val GoldLight = Color(0xFFE8CF92)
val GoldDark = Color(0xFF8A6A2E)

// Forest / charcoal (dark)
val Forest950 = Color(0xFF0C1310)
val Forest900 = Color(0xFF111A16)
val Forest800 = Color(0xFF18231E)
val Forest700 = Color(0xFF223029)
val Forest600 = Color(0xFF2E3E35)
val Parchment = Color(0xFFEDE3CC)
val ParchmentMuted = Color(0xFFA9A08B)

// Parchment (light)
val Linen = Color(0xFFF1E9D8)
val LinenSurface = Color(0xFFF9F4E8)
val LinenVariant = Color(0xFFE5D9C0)
val WoodInk = Color(0xFF2A2119)
val WoodMuted = Color(0xFF6E604D)

// Players
val CrimsonBase = Color(0xFF9E3B34)
val CrimsonLight = Color(0xFFD07A55)
val CrimsonDark = Color(0xFF571C18)
val TealBase = Color(0xFF1F6B72)
val TealLight = Color(0xFF55A8AE)
val TealDark = Color(0xFF0E383D)

data class PlayerColors(val base: Color, val light: Color, val dark: Color)

val Player.colors: PlayerColors
    get() = when (this) {
        Player.PLAYER_ONE -> PlayerColors(CrimsonBase, CrimsonLight, CrimsonDark)
        Player.PLAYER_TWO -> PlayerColors(TealBase, TealLight, TealDark)
    }

/** Colors for the Canvas board, independent of the app's light/dark theme. */
data class BoardPalette(
    val frameOuter: Color,
    val frameInner: Color,
    val inlay: Color,
    val field: Color,
    val fieldGlow: Color,
    val path: Color,
    val tileTop: Color,
    val tileBottom: Color,
    val tileEdge: Color,
    val tileShadow: Color,
    val trapTop: Color,
    val trapBottom: Color,
    val ink: Color,
    val label: Color,
    val coin: Color,
    val coinShade: Color,
    val gem: Color,
    val gemLight: Color,
    val chestWood: Color,
    val chestGold: Color,
    val trap: Color,
    val bonus: Color,
    val start: Color,
    val emblem: Color
)

val ClassicBoard = BoardPalette(
    frameOuter = Color(0xFF3B2A1B),
    frameInner = Color(0xFF24190F),
    inlay = Color(0xFFB8934E),
    field = Color(0xFF1F2D25),
    fieldGlow = Color(0xFF2F4235),
    path = Color(0x66C9A45C),
    tileTop = Color(0xFFE9DCBD),
    tileBottom = Color(0xFFCDB98E),
    tileEdge = Color(0xFF8F7A52),
    tileShadow = Color(0x66000000),
    trapTop = Color(0xFFD2B8A0),
    trapBottom = Color(0xFFB0927A),
    ink = Color(0xFF3A2C1E),
    label = Color(0xFF5A4630),
    coin = Color(0xFFC99A3E),
    coinShade = Color(0xFF8C6723),
    gem = Color(0xFF2E7458),
    gemLight = Color(0xFF6FB693),
    chestWood = Color(0xFF6B4527),
    chestGold = Color(0xFFD4AF5E),
    trap = Color(0xFF8B3A2C),
    bonus = Color(0xFF3F6E66),
    start = Color(0xFF8A6A2E),
    emblem = Color(0x33C9A45C)
)

val MidnightBoard = BoardPalette(
    frameOuter = Color(0xFF1C2130),
    frameInner = Color(0xFF10131C),
    inlay = Color(0xFF9AA7C0),
    field = Color(0xFF151C29),
    fieldGlow = Color(0xFF223047),
    path = Color(0x66D8B56A),
    tileTop = Color(0xFF55627A),
    tileBottom = Color(0xFF3B4659),
    tileEdge = Color(0xFF7D8AA3),
    tileShadow = Color(0x88000000),
    trapTop = Color(0xFF5E4A55),
    trapBottom = Color(0xFF45353F),
    ink = Color(0xFFE6DCC6),
    label = Color(0xFFD1C7B0),
    coin = Color(0xFFD8B56A),
    coinShade = Color(0xFF9C7B35),
    gem = Color(0xFF4FA5A0),
    gemLight = Color(0xFF9FE0D6),
    chestWood = Color(0xFF3A2E2A),
    chestGold = Color(0xFFE2C47C),
    trap = Color(0xFFE08A70),
    bonus = Color(0xFF9CC7D8),
    start = Color(0xFFD8B56A),
    emblem = Color(0x33D8B56A)
)

val BoardTheme.palette: BoardPalette
    get() = when (this) {
        BoardTheme.CLASSIC -> ClassicBoard
        BoardTheme.MIDNIGHT -> MidnightBoard
    }
