package ph.jeepfare.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamBorderWidth
import ph.jeepfare.ui.theme.PamTone
import ph.jeepfare.ui.theme.deepOf
import ph.jeepfare.ui.theme.overline
import ph.jeepfare.ui.theme.tintOf

/** Motion: quick and physical — 160ms cubic-bezier(.2,.7,.3,1), press scale. */
val PamEasing = CubicBezierEasing(0.2f, 0.7f, 0.3f, 1f)

@Composable
private fun pressScale(pressed: Boolean, target: Float): Float {
    val scale by animateFloatAsState(
        targetValue = if (pressed) target else 1f,
        animationSpec = tween(durationMillis = 160, easing = PamEasing),
    )
    return scale
}

/** Tricolor signage stripe (tokens --stripe): red/yellow/blue repeating band. */
@Composable
fun PamStripe(modifier: Modifier = Modifier, height: Dp = 6.dp, segment: Dp = 14.dp) {
    val pal = LocalPamPalette.current
    val colors = listOf(pal.red, pal.yellow, pal.blue)
    androidx.compose.foundation.Canvas(modifier = modifier.height(height)) {
        val seg = segment.toPx()
        var x = 0f
        var i = 0
        while (x < size.width) {
            drawRect(colors[i % 3], topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                size = androidx.compose.ui.geometry.Size(minOf(seg, size.width - x), size.height))
            x += seg; i++
        }
    }
}

enum class PamButtonVariant { PRIMARY, SECONDARY, YELLOW, GHOST }
enum class PamButtonSize { SM, MD, LG }

@Composable
fun PamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: PamButtonVariant = PamButtonVariant.PRIMARY,
    size: PamButtonSize = PamButtonSize.MD,
    enabled: Boolean = true,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = pressScale(pressed && enabled, 0.97f)

    val height = when (size) { PamButtonSize.SM -> 40.dp; PamButtonSize.MD -> 48.dp; PamButtonSize.LG -> 56.dp }
    val fontSize = when (size) { PamButtonSize.SM -> 14.sp; PamButtonSize.MD -> 15.sp; PamButtonSize.LG -> 17.sp }
    val hPad = if (size == PamButtonSize.SM) 16.dp else 22.dp
    val iconSize = if (size == PamButtonSize.SM) 18.dp else 20.dp

    val bg: Color; val fg: Color; val borderColor: Color?
    when (variant) {
        PamButtonVariant.PRIMARY -> { bg = if (pressed) pal.actionPress else pal.action; fg = pal.actionInk; borderColor = null }
        PamButtonVariant.YELLOW -> { bg = if (pressed) pal.yellowDeep else pal.yellow; fg = Color(0xFF271F18); borderColor = null }
        PamButtonVariant.SECONDARY -> { bg = if (pressed) pal.bg2 else pal.surface; fg = pal.ink; borderColor = pal.line2 }
        PamButtonVariant.GHOST -> { bg = if (pressed) pal.bg2 else Color.Transparent; fg = pal.blue; borderColor = null }
    }

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (enabled) 1f else 0.4f)
            .height(height)
            .background(bg, CircleShape)
            .then(if (borderColor != null) Modifier.border(PamBorderWidth, borderColor, CircleShape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = hPad),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(iconSize))
        Text(text, fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = fontSize, color = fg, maxLines = 1)
    }
}

enum class PamIconButtonVariant { PLAIN, TONAL, FILLED }

@Composable
fun PamIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PamIconButtonVariant = PamIconButtonVariant.PLAIN,
    size: Dp = 48.dp,
) {
    val pal = LocalPamPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = pressScale(pressed, 0.94f)

    val bg: Color; val fg: Color; val borderColor: Color?
    when (variant) {
        PamIconButtonVariant.PLAIN -> { bg = if (pressed) pal.bg2 else pal.surface; fg = pal.ink2; borderColor = pal.line }
        PamIconButtonVariant.TONAL -> { bg = pal.bg2; fg = pal.ink; borderColor = null }
        PamIconButtonVariant.FILLED -> { bg = if (pressed) pal.actionPress else pal.action; fg = Color.White; borderColor = null }
    }
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .size(size)
            .background(bg, CircleShape)
            .then(if (borderColor != null) Modifier.border(PamBorderWidth, borderColor, CircleShape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = fg, modifier = Modifier.size(size * 0.46f))
    }
}

/** Section overline label: 11.5sp, 800, caps, +0.06em, ink-3. */
@Composable
fun PamOverline(text: String, modifier: Modifier = Modifier) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Text(text.uppercase(), style = fonts.overline(), color = pal.ink3, modifier = modifier)
}

/** White card on cream: 1.5dp line border, radius 20, optional overline and stripe trim. */
@Composable
fun PamCard(
    modifier: Modifier = Modifier,
    overline: String? = null,
    stripe: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val pal = LocalPamPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = pal.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(PamBorderWidth, pal.line),
        shadowElevation = 1.dp,
    ) {
        Column {
            if (stripe) PamStripe(Modifier.fillMaxWidth())
            Column(Modifier.padding(contentPadding)) {
                if (overline != null) PamOverline(overline, Modifier.padding(bottom = 10.dp))
                content()
            }
        }
    }
}

/** Tinted pill chip; tones map to signage colors. */
@Composable
fun PamChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: PamTone = PamTone.NEUTRAL,
    icon: ImageVector? = null,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = modifier
            .height(28.dp)
            .background(pal.tintOf(tone), CircleShape)
            .padding(horizontal = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = pal.deepOf(tone), modifier = Modifier.size(14.dp))
        Text(text, fontFamily = fonts.body, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = pal.deepOf(tone), maxLines = 1)
    }
}

/** Tinted info strip (info / estimate / ok / alert). */
@Composable
fun PamCallout(
    text: String,
    modifier: Modifier = Modifier,
    tone: PamTone = PamTone.BLUE,
    icon: ImageVector,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(pal.tintOf(tone), RoundedCornerShape(14.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = pal.deepOf(tone), modifier = Modifier.size(19.dp).padding(top = 1.dp))
        Text(text, fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.9.sp, color = pal.ink)
    }
}

/** Passenger count row: tinted icon box, label + discount note, − count +. */
@Composable
fun PamStepper(
    icon: ImageVector,
    tone: PamTone,
    label: String,
    chip: String?,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0,
    max: Int = 30,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(pal.tintOf(tone), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = pal.deepOf(tone), modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, fontFamily = fonts.body, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 18.sp, color = pal.ink)
            if (chip != null) {
                Text(chip, fontFamily = fonts.body, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, color = pal.deepOf(tone))
            }
        }
        StepperButton(ph.jeepfare.ui.theme.PamIcons.Remove, "bawasan $label", enabled = value > min) { onValueChange(value - 1) }
        Text(
            "$value",
            fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = pal.ink,
            textAlign = TextAlign.Center, modifier = Modifier.width(34.dp),
        )
        StepperButton(ph.jeepfare.ui.theme.PamIcons.Add, "dagdagan $label", enabled = value < max) { onValueChange(value + 1) }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, contentDescription: String, enabled: Boolean, onClick: () -> Unit) {
    val pal = LocalPamPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = pressScale(pressed && enabled, 0.94f)
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (enabled) 1f else 0.4f)
            .size(44.dp)
            .background(if (pressed && enabled) pal.bg2 else pal.surface, CircleShape)
            .border(PamBorderWidth, pal.line2, CircleShape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = if (enabled) pal.ink else pal.ink3, modifier = Modifier.size(20.dp))
    }
}

data class PamSegmentItem<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val iconTone: PamTone = PamTone.RED,
    val sub: String? = null,
)

/** Cream track with a white selected pill. */
@Composable
fun <T> PamSegmented(
    items: List<PamSegmentItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(pal.bg2, CircleShape)
            .border(PamBorderWidth, pal.line, CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            val sel = item.value == selected
            Surface(
                modifier = Modifier.weight(1f).height(if (item.sub != null) 52.dp else 44.dp),
                color = if (sel) pal.surface else Color.Transparent,
                shape = CircleShape,
                border = if (sel) androidx.compose.foundation.BorderStroke(PamBorderWidth, pal.line) else null,
                shadowElevation = if (sel) 1.dp else 0.dp,
                onClick = { onSelect(item.value) },
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.icon != null) {
                            Icon(
                                item.icon, contentDescription = null,
                                tint = if (sel) pal.deepOf(item.iconTone) else pal.ink3,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            item.label,
                            fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            color = if (sel) pal.ink else pal.ink2, maxLines = 1,
                        )
                    }
                    if (item.sub != null) {
                        Text(item.sub, fontFamily = fonts.mono, fontSize = 11.sp, color = pal.ink3, maxLines = 1)
                    }
                }
            }
        }
    }
}
