package com.example.treasureboard.ui.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import com.example.treasureboard.game.Board
import com.example.treasureboard.game.Player
import com.example.treasureboard.game.SpaceType
import com.example.treasureboard.ui.theme.BoardPalette
import com.example.treasureboard.ui.theme.GoldLight
import com.example.treasureboard.ui.theme.Parchment
import com.example.treasureboard.ui.theme.colors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Wooden frame, gold inlay, felt field, center emblem and the track line joining the spaces. */
fun DrawScope.drawBoardBase(board: Board, palette: BoardPalette) {
    val w = size.minDimension
    val cell = w / Board.GRID
    val corner = CornerRadius(cell * 0.45f)

    // Frame
    drawRoundRect(
        brush = Brush.linearGradient(listOf(palette.frameOuter, palette.frameInner), Offset.Zero, Offset(w, w)),
        cornerRadius = corner
    )
    drawRoundRect(
        color = palette.inlay.copy(alpha = 0.55f),
        topLeft = Offset(cell * 0.06f, cell * 0.06f),
        size = Size(w - cell * 0.12f, w - cell * 0.12f),
        cornerRadius = CornerRadius(cell * 0.4f),
        style = Stroke(width = cell * 0.02f)
    )

    // Inner field
    val fieldInset = cell * 0.98f
    val fieldSize = Size(w - fieldInset * 2, w - fieldInset * 2)
    drawRoundRect(
        brush = Brush.radialGradient(
            listOf(palette.fieldGlow, palette.field),
            center = Offset(w / 2, w * 0.42f),
            radius = w * 0.45f
        ),
        topLeft = Offset(fieldInset, fieldInset),
        size = fieldSize,
        cornerRadius = CornerRadius(cell * 0.3f)
    )
    drawRoundRect(
        color = palette.inlay.copy(alpha = 0.45f),
        topLeft = Offset(fieldInset + cell * 0.12f, fieldInset + cell * 0.12f),
        size = Size(fieldSize.width - cell * 0.24f, fieldSize.height - cell * 0.24f),
        cornerRadius = CornerRadius(cell * 0.22f),
        style = Stroke(width = cell * 0.018f)
    )
    drawCompassEmblem(Offset(w / 2, w / 2), w * 0.26f, palette.emblem)

    // Track line through every space centre
    val track = Path().apply {
        board.spaces.forEachIndexed { i, s ->
            val p = Offset(s.x * w, s.y * w)
            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
        close()
    }
    drawPath(track, palette.path, style = Stroke(width = cell * 0.08f, join = StrokeJoin.Round))
}

private fun DrawScope.drawCompassEmblem(center: Offset, radius: Float, color: Color) {
    drawCircle(color, radius, center, style = Stroke(width = radius * 0.02f))
    drawCircle(color, radius * 0.82f, center, style = Stroke(width = radius * 0.012f))
    val star = Path()
    for (i in 0 until 16) {
        val angle = -PI / 2 + i * PI / 8
        val r = when {
            i % 4 == 0 -> radius * 0.95f
            i % 2 == 0 -> radius * 0.5f
            else -> radius * 0.16f
        }
        val x = center.x + (cos(angle) * r).toFloat()
        val y = center.y + (sin(angle) * r).toFloat()
        if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
    }
    star.close()
    drawPath(star, color)
    for (i in 0 until 32) {
        val angle = i * PI / 16
        val inner = if (i % 4 == 0) radius * 0.86f else radius * 0.9f
        drawLine(
            color,
            Offset(center.x + (cos(angle) * inner).toFloat(), center.y + (sin(angle) * inner).toFloat()),
            Offset(center.x + (cos(angle) * radius).toFloat(), center.y + (sin(angle) * radius).toFloat()),
            strokeWidth = radius * 0.012f
        )
    }
}

/** Unit direction from a space towards the next one, used to orient Bonus arrows. */
fun Board.directionAt(index: Int): Offset {
    val a = this[index]
    val b = this[(index + 1) % size]
    return Offset(b.x - a.x, b.y - a.y)
}

fun DrawScope.drawTile(
    type: SpaceType,
    center: Offset,
    cell: Float,
    palette: BoardPalette,
    direction: Offset,
    label: TextLayoutResult?
) {
    val half = cell * 0.43f
    val rect = Rect(center.x - half, center.y - half, center.x + half, center.y + half)
    val radius = CornerRadius(cell * 0.14f)
    val isTrap = type == SpaceType.TRAP

    // Drop shadow + raised edge give a stone-tile feel under soft top-left light.
    drawRoundRect(
        palette.tileShadow,
        topLeft = rect.topLeft + Offset(cell * 0.03f, cell * 0.06f),
        size = rect.size,
        cornerRadius = radius
    )
    drawRoundRect(
        palette.tileEdge,
        topLeft = rect.topLeft + Offset(0f, cell * 0.03f),
        size = rect.size,
        cornerRadius = radius
    )
    drawRoundRect(
        Brush.verticalGradient(
            if (isTrap) listOf(palette.trapTop, palette.trapBottom) else listOf(palette.tileTop, palette.tileBottom),
            startY = rect.top,
            endY = rect.bottom
        ),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = radius
    )
    drawRoundRect(
        Color.White.copy(alpha = 0.18f),
        topLeft = rect.topLeft + Offset(cell * 0.03f, cell * 0.03f),
        size = Size(rect.width - cell * 0.06f, rect.height - cell * 0.06f),
        cornerRadius = CornerRadius(cell * 0.11f),
        style = Stroke(width = cell * 0.015f)
    )

    when (type) {
        SpaceType.START -> {
            drawRoundRect(
                palette.start,
                topLeft = rect.topLeft,
                size = rect.size,
                cornerRadius = radius,
                style = Stroke(width = cell * 0.06f)
            )
            drawRoundRect(
                palette.start.copy(alpha = 0.6f),
                topLeft = rect.topLeft + Offset(cell * 0.08f, cell * 0.08f),
                size = Size(rect.width - cell * 0.16f, rect.height - cell * 0.16f),
                cornerRadius = CornerRadius(cell * 0.08f),
                style = Stroke(width = cell * 0.015f)
            )
        }
        SpaceType.TRAP -> drawCracks(rect, cell, palette.trap.copy(alpha = 0.45f))
        SpaceType.GOLD_CHEST -> drawRoundRect(
            palette.chestGold.copy(alpha = 0.8f),
            topLeft = rect.topLeft,
            size = rect.size,
            cornerRadius = radius,
            style = Stroke(width = cell * 0.035f)
        )
        else -> Unit
    }

    val symbolCenter = if (label != null && type != SpaceType.START) center - Offset(0f, cell * 0.08f) else center
    val symbolRadius = if (label != null) cell * 0.25f else cell * 0.3f
    drawSpaceSymbol(type, symbolCenter, symbolRadius, palette, direction)

    if (label != null) {
        val y = if (type == SpaceType.START) center.y - label.size.height / 2f else rect.bottom - label.size.height - cell * 0.04f
        drawText(label, topLeft = Offset(center.x - label.size.width / 2f, y))
    }
}

private fun DrawScope.drawCracks(rect: Rect, cell: Float, color: Color) {
    val crack = Path().apply {
        moveTo(rect.left + rect.width * 0.08f, rect.top + rect.height * 0.22f)
        lineTo(rect.left + rect.width * 0.24f, rect.top + rect.height * 0.3f)
        lineTo(rect.left + rect.width * 0.2f, rect.top + rect.height * 0.44f)
        moveTo(rect.right - rect.width * 0.06f, rect.bottom - rect.height * 0.3f)
        lineTo(rect.right - rect.width * 0.22f, rect.bottom - rect.height * 0.22f)
        lineTo(rect.right - rect.width * 0.18f, rect.bottom - rect.height * 0.08f)
    }
    drawPath(crack, color, style = Stroke(width = cell * 0.02f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** Draws a space's original vector symbol within a circle of [r] around [c]. */
fun DrawScope.drawSpaceSymbol(type: SpaceType, c: Offset, r: Float, palette: BoardPalette, direction: Offset) {
    when (type) {
        SpaceType.NORMAL -> drawCircle(palette.ink.copy(alpha = 0.22f), r * 0.14f, c)
        SpaceType.START -> Unit
        SpaceType.COIN -> drawCoin(c, r, palette)
        SpaceType.TREASURE -> drawGem(c, r, palette)
        SpaceType.GOLD_CHEST -> drawChest(c, r, palette)
        SpaceType.TRAP -> drawWarning(c, r, palette)
        SpaceType.BONUS_STEP -> drawChevrons(c, r, palette.bonus, direction)
    }
}

private fun DrawScope.drawCoin(c: Offset, r: Float, p: BoardPalette) {
    val cr = r * 0.62f
    drawCircle(p.coinShade, cr, c + Offset(0f, r * 0.1f))
    drawCircle(
        Brush.radialGradient(listOf(GoldLight, p.coin), center = c - Offset(cr * 0.35f, cr * 0.35f), radius = cr * 1.4f),
        cr,
        c
    )
    drawCircle(p.coinShade, cr * 0.7f, c, style = Stroke(width = r * 0.07f))
    drawLine(p.coinShade, c - Offset(0f, cr * 0.36f), c + Offset(0f, cr * 0.36f), strokeWidth = r * 0.1f, cap = StrokeCap.Round)
}

private fun DrawScope.drawGem(c: Offset, r: Float, p: BoardPalette) {
    fun pt(x: Float, y: Float) = Offset(c.x + x * r, c.y + y * r)
    val tl = pt(-0.3f, -0.52f); val tr = pt(0.3f, -0.52f)
    val gl = pt(-0.64f, -0.16f); val gr = pt(0.64f, -0.16f)
    val bottom = pt(0f, 0.66f)
    val gem = Path().apply {
        moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(gr.x, gr.y); lineTo(bottom.x, bottom.y); lineTo(gl.x, gl.y); close()
    }
    drawPath(gem, Brush.linearGradient(listOf(p.gemLight, p.gem), tl, bottom))
    val facet = p.gem.copy(alpha = 0.9f)
    val fw = r * 0.05f
    drawLine(facet, gl, gr, fw)
    val il = pt(-0.14f, -0.16f); val ir = pt(0.14f, -0.16f)
    drawLine(facet, tl, il, fw); drawLine(facet, il, bottom, fw)
    drawLine(facet, tr, ir, fw); drawLine(facet, ir, bottom, fw)
    val shine = Path().apply { moveTo(tl.x, tl.y); lineTo(il.x, il.y); lineTo(gl.x, gl.y); close() }
    drawPath(shine, Color.White.copy(alpha = 0.35f))
    drawPath(gem, p.ink.copy(alpha = 0.55f), style = Stroke(width = r * 0.06f, join = StrokeJoin.Round))
}

private fun DrawScope.drawChest(c: Offset, r: Float, p: BoardPalette) {
    drawCircle(Brush.radialGradient(listOf(p.chestGold.copy(alpha = 0.45f), Color.Transparent), c, r * 1.1f), r * 1.1f, c)
    val left = c.x - r * 0.66f; val right = c.x + r * 0.66f
    val seam = c.y - r * 0.02f; val bottom = c.y + r * 0.56f; val top = c.y - r * 0.52f
    // Body
    drawRoundRect(p.chestWood, Offset(left, seam), Size(right - left, bottom - seam), CornerRadius(r * 0.08f))
    // Arched lid
    val lid = Path().apply {
        moveTo(left, seam)
        lineTo(left, c.y - r * 0.22f)
        cubicTo(left, top, right, top, right, c.y - r * 0.22f)
        lineTo(right, seam)
        close()
    }
    drawPath(lid, Brush.verticalGradient(listOf(p.chestWood.copy(alpha = 0.85f), p.chestWood), top, seam))
    // Gold bands, trim and lock
    val band = r * 0.12f
    listOf(-0.38f, 0.38f).forEach { fx ->
        val x = c.x + fx * r - band / 2
        drawRect(p.chestGold, Offset(x, c.y - r * 0.4f), Size(band, bottom - (c.y - r * 0.4f)))
    }
    drawRect(p.chestGold, Offset(left, seam - r * 0.05f), Size(right - left, r * 0.1f))
    drawRoundRect(p.chestGold, Offset(c.x - r * 0.14f, seam - r * 0.12f), Size(r * 0.28f, r * 0.32f), CornerRadius(r * 0.05f))
    drawCircle(p.chestWood, r * 0.05f, Offset(c.x, seam + r * 0.02f))
    drawPath(lid, p.ink.copy(alpha = 0.5f), style = Stroke(width = r * 0.05f))
}

private fun DrawScope.drawWarning(c: Offset, r: Float, p: BoardPalette) {
    val tri = Path().apply {
        moveTo(c.x, c.y - r * 0.62f)
        lineTo(c.x + r * 0.66f, c.y + r * 0.5f)
        lineTo(c.x - r * 0.66f, c.y + r * 0.5f)
        close()
    }
    drawPath(tri, p.trap.copy(alpha = 0.12f))
    drawPath(tri, p.trap, style = Stroke(width = r * 0.13f, join = StrokeJoin.Round))
    drawLine(p.trap, Offset(c.x, c.y - r * 0.24f), Offset(c.x, c.y + r * 0.12f), r * 0.13f, StrokeCap.Round)
    drawCircle(p.trap, r * 0.08f, Offset(c.x, c.y + r * 0.32f))
}

private fun DrawScope.drawChevrons(c: Offset, r: Float, color: Color, direction: Offset) {
    val degrees = (atan2(direction.y, direction.x) * 180f / PI.toFloat())
    rotate(degrees, pivot = c) {
        listOf(-0.3f, 0.2f).forEach { dx ->
            val chevron = Path().apply {
                moveTo(c.x + (dx - 0.22f) * r, c.y - 0.42f * r)
                lineTo(c.x + (dx + 0.18f) * r, c.y)
                lineTo(c.x + (dx - 0.22f) * r, c.y + 0.42f * r)
            }
            drawPath(chevron, color, style = Stroke(width = r * 0.17f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/**
 * A raised, polished token. Player 1 carries a diamond, Player 2 a circle, so the two
 * never rely on colour alone.
 */
fun DrawScope.drawToken(
    player: Player,
    center: Offset,
    radius: Float,
    lift: Float = 0f,
    glow: Float = 0f
) {
    val colors = player.colors
    val raised = center - Offset(0f, lift * radius * 0.9f)

    drawOval(
        Color.Black.copy(alpha = 0.38f * (1f - lift * 0.5f)),
        topLeft = Offset(center.x - radius * (0.95f - lift * 0.15f), center.y + radius * 0.35f),
        size = Size(radius * 2f * (0.95f - lift * 0.15f), radius * 0.7f)
    )
    if (glow > 0f) {
        drawCircle(
            Brush.radialGradient(
                listOf(GoldLight.copy(alpha = 0.55f * glow), Color.Transparent),
                center = raised,
                radius = radius * 1.7f
            ),
            radius * 1.7f,
            raised
        )
        drawCircle(GoldLight.copy(alpha = 0.85f * glow), radius * 1.25f, raised, style = Stroke(width = radius * 0.08f))
    }
    // Side wall, then face
    drawCircle(colors.dark, radius, raised + Offset(0f, radius * 0.16f))
    drawCircle(
        Brush.radialGradient(
            listOf(colors.light, colors.base, colors.dark),
            center = raised - Offset(radius * 0.35f, radius * 0.4f),
            radius = radius * 1.6f
        ),
        radius,
        raised
    )
    drawCircle(GoldLight.copy(alpha = 0.85f), radius * 0.9f, raised, style = Stroke(width = radius * 0.08f))

    val emblem = Brush.linearGradient(
        listOf(Color.White, Parchment),
        raised - Offset(radius * 0.4f, radius * 0.4f),
        raised + Offset(radius * 0.4f, radius * 0.4f)
    )
    translate(raised.x, raised.y) {
        when (player) {
            Player.PLAYER_ONE -> {
                val d = Path().apply {
                    moveTo(0f, -radius * 0.48f); lineTo(radius * 0.36f, 0f)
                    lineTo(0f, radius * 0.48f); lineTo(-radius * 0.36f, 0f); close()
                }
                drawPath(d, emblem)
                drawPath(d, colors.dark.copy(alpha = 0.5f), style = Stroke(width = radius * 0.05f))
            }
            Player.PLAYER_TWO -> {
                drawCircle(emblem, radius * 0.4f, Offset.Zero)
                drawCircle(colors.base, radius * 0.2f, Offset.Zero, style = Stroke(width = radius * 0.07f))
            }
        }
    }
    // Specular highlight
    drawOval(
        Color.White.copy(alpha = 0.22f),
        topLeft = raised - Offset(radius * 0.62f, radius * 0.78f),
        size = Size(radius * 0.8f, radius * 0.42f)
    )
}

/** Short effect label printed on a tile, or null for spaces without one. */
fun SpaceType.shortLabel(): String? = when (this) {
    SpaceType.START -> "START"
    SpaceType.COIN -> "+2"
    SpaceType.TREASURE -> "+5"
    SpaceType.GOLD_CHEST -> "+8"
    SpaceType.TRAP -> "−3"
    SpaceType.BONUS_STEP -> "»2"
    SpaceType.NORMAL -> null
}
