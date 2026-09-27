package com.example.treasureboard.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.treasureboard.game.Player
import com.example.treasureboard.ui.theme.Gold
import com.example.treasureboard.ui.theme.GoldDark
import com.example.treasureboard.ui.theme.GoldLight
import com.example.treasureboard.ui.theme.LocalBackdrop
import com.example.treasureboard.ui.theme.colors

/** Minimal vector icons built from path data, so no icon library is required. */
object TbIcons {
    val Back: ImageVector by lazy { icon("M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z") }
    val Home: ImageVector by lazy { icon("M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z") }
    val Restart: ImageVector by lazy {
        icon(
            "M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 " +
                "7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 " +
                "4.22,1.78L13,11h7V4l-2.35,2.35z"
        )
    }

    private fun icon(path: String) = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f
    ).addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black)).build()
}

/** Full-screen themed backdrop that respects system bars. */
@Composable
fun ScreenBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(LocalBackdrop.current.brush)
            .safeDrawingPadding(),
        content = content
    )
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(TbIcons.Back, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Color(0xFF1E160A),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(1.dp, if (enabled) GoldLight.copy(alpha = 0.7f) else Color.Transparent)
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun QuietButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** A quiet surface with a hairline gold edge, used for panels and settings groups. */
@Composable
fun PanelCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/** Ornamental gold divider: line – diamond – line. */
@Composable
fun OrnamentDivider(modifier: Modifier = Modifier, width: Dp = 160.dp) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier.size(width, 12.dp)) {
        val cy = size.height / 2
        val cx = size.width / 2
        drawLine(
            Brush.horizontalGradient(listOf(Color.Transparent, color), 0f, cx - 10.dp.toPx()),
            Offset(0f, cy), Offset(cx - 10.dp.toPx(), cy), strokeWidth = 1.dp.toPx()
        )
        drawLine(
            Brush.horizontalGradient(listOf(color, Color.Transparent), cx + 10.dp.toPx(), size.width),
            Offset(cx + 10.dp.toPx(), cy), Offset(size.width, cy), strokeWidth = 1.dp.toPx()
        )
        val d = 5.dp.toPx()
        drawPath(Path().apply {
            moveTo(cx, cy - d); lineTo(cx + d, cy); lineTo(cx, cy + d); lineTo(cx - d, cy); close()
        }, color)
    }
}

/** The player's colour plus distinct shape (diamond / circle) for badges and headings. */
@Composable
fun PlayerSymbol(player: Player, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    val colors = player.colors
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2
        val c = center
        drawCircle(Brush.radialGradient(listOf(colors.light, colors.base, colors.dark), c - Offset(r * 0.3f, r * 0.3f), r * 1.5f), r, c)
        drawCircle(GoldLight.copy(alpha = 0.8f), r * 0.9f, c, style = Stroke(width = r * 0.12f))
        when (player) {
            Player.PLAYER_ONE -> drawPath(Path().apply {
                moveTo(c.x, c.y - r * 0.5f); lineTo(c.x + r * 0.36f, c.y)
                lineTo(c.x, c.y + r * 0.5f); lineTo(c.x - r * 0.36f, c.y); close()
            }, Color.White)
            Player.PLAYER_TWO -> drawCircle(Color.White, r * 0.36f, c)
        }
    }
}

/** An ivory die face drawn on Canvas; wobbles while [rolling]. */
@Composable
fun DieView(face: Int, rolling: Boolean, modifier: Modifier = Modifier, size: Dp = 64.dp, accent: Color = Gold) {
    val wobble by rememberInfiniteTransition(label = "die").animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(90), RepeatMode.Reverse),
        label = "wobble"
    )
    val pip = Color(0xFF2B2016)
    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = if (rolling) "Dice rolling" else "Dice rolled $face" }
    ) {
        val s = this.size.minDimension
        val inset = s * 0.08f
        val box = Size(s - inset * 2, s - inset * 2)
        rotate(if (rolling) wobble else 0f) {
            drawRoundRect(
                Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(inset + s * 0.02f, inset + s * 0.06f),
                size = box,
                cornerRadius = CornerRadius(s * 0.2f)
            )
            drawRoundRect(
                Brush.linearGradient(
                    listOf(Color(0xFFFBF5E6), Color(0xFFE6D9BC)),
                    Offset(inset, inset),
                    Offset(s - inset, s - inset)
                ),
                topLeft = Offset(inset, inset),
                size = box,
                cornerRadius = CornerRadius(s * 0.2f)
            )
            drawRoundRect(
                accent.copy(alpha = 0.9f),
                topLeft = Offset(inset, inset),
                size = box,
                cornerRadius = CornerRadius(s * 0.2f),
                style = Stroke(width = s * 0.03f)
            )
            val step = box.width / 4
            val left = inset + step; val mid = inset + step * 2; val right = inset + step * 3
            val pips = when (face) {
                1 -> listOf(mid to mid)
                2 -> listOf(left to left, right to right)
                3 -> listOf(left to left, mid to mid, right to right)
                4 -> listOf(left to left, right to left, left to right, right to right)
                5 -> listOf(left to left, right to left, mid to mid, left to right, right to right)
                else -> listOf(left to left, right to left, left to mid, right to mid, left to right, right to right)
            }
            pips.forEach { (x, y) ->
                drawCircle(pip, s * 0.075f, Offset(x, y))
                drawCircle(Color.White.copy(alpha = 0.25f), s * 0.03f, Offset(x - s * 0.02f, y - s * 0.02f))
            }
        }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(2.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Simple segmented chooser built from stable APIs. */
@Composable
fun <T> SegmentedChoice(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Surface(
                onClick = { onSelect(value) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

val GoldGradient = Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))
