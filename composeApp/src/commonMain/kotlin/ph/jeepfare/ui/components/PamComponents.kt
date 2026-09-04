package ph.jeepfare.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.clipToBounds
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
import ph.jeepfare.ui.theme.PamMotion
import ph.jeepfare.ui.theme.PamTone
import ph.jeepfare.ui.theme.pamCounterSwap
import ph.jeepfare.ui.theme.pamIconSwap
import ph.jeepfare.ui.theme.pamSwap
import ph.jeepfare.ui.theme.baseOf
import ph.jeepfare.ui.theme.deepOf
import ph.jeepfare.ui.theme.overline
import ph.jeepfare.ui.theme.tintOf

@Composable
private fun pressScale(pressed: Boolean, target: Float): Float {
    // Press dips on the design system's 160ms ease; release springs back, so a
    // tap feels like a button letting go rather than a value snapping.
    val scale by animateFloatAsState(
        targetValue = if (pressed) target else 1f,
        animationSpec = if (pressed) PamMotion.press() else PamMotion.bouncy(),
        label = "pressScale",
    )
    return scale
}

/** Enabled/disabled dimming, animated so a control never blinks in or out. */
@Composable
private fun enabledAlpha(enabled: Boolean): Float {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.4f,
        animationSpec = PamMotion.quick(),
        label = "enabledAlpha",
    )
    return alpha
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

    val bgTarget: Color; val fg: Color; val borderColor: Color?
    when (variant) {
        PamButtonVariant.PRIMARY -> { bgTarget = if (pressed) pal.actionPress else pal.action; fg = pal.actionInk; borderColor = null }
        PamButtonVariant.YELLOW -> { bgTarget = if (pressed) pal.yellowDeep else pal.yellow; fg = Color(0xFF271F18); borderColor = null }
        PamButtonVariant.SECONDARY -> { bgTarget = if (pressed) pal.bg2 else pal.surface; fg = pal.ink; borderColor = pal.line2 }
        PamButtonVariant.GHOST -> { bgTarget = if (pressed) pal.bg2 else Color.Transparent; fg = pal.blue; borderColor = null }
    }
    // The fill blends instead of cutting — a pressed button that snaps color
    // reads as a redraw, one that blends reads as a surface being pushed.
    val bg by animateColorAsState(bgTarget, animationSpec = PamMotion.press(), label = "buttonBg")
    val alpha = enabledAlpha(enabled)

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            // Label swaps ("Save" -> "Saved!") resize the pill smoothly.
            .animateContentSize(animationSpec = PamMotion.spatial())
            .height(height)
            .background(bg, CircleShape)
            .then(if (borderColor != null) Modifier.border(PamBorderWidth, borderColor, CircleShape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = hPad),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AnimatedContent(
                targetState = icon,
                transitionSpec = { pamSwap() },
                contentAlignment = Alignment.Center,
                label = "buttonIcon",
            ) { current ->
                Icon(current, contentDescription = null, tint = fg, modifier = Modifier.size(iconSize))
            }
        }
        AnimatedContent(
            targetState = text,
            transitionSpec = { pamSwap() },
            contentAlignment = Alignment.Center,
            label = "buttonLabel",
        ) { current ->
            Text(
                current,
                fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = fontSize,
                color = fg, maxLines = 1,
            )
        }
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

    val bgTarget: Color; val fg: Color; val borderColor: Color?
    when (variant) {
        PamIconButtonVariant.PLAIN -> { bgTarget = if (pressed) pal.bg2 else pal.surface; fg = pal.ink2; borderColor = pal.line }
        PamIconButtonVariant.TONAL -> { bgTarget = pal.bg2; fg = pal.ink; borderColor = null }
        PamIconButtonVariant.FILLED -> { bgTarget = if (pressed) pal.actionPress else pal.action; fg = Color.White; borderColor = null }
    }
    val bg by animateColorAsState(bgTarget, animationSpec = PamMotion.press(), label = "iconButtonBg")
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .size(size)
            .background(bg, CircleShape)
            .then(if (borderColor != null) Modifier.border(PamBorderWidth, borderColor, CircleShape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        // Swapping the glyph (sun <-> moon) turns and fades rather than cutting.
        AnimatedContent(
            targetState = icon,
            transitionSpec = { pamIconSwap() },
            contentAlignment = Alignment.Center,
            label = "iconButtonIcon",
        ) { current ->
            Icon(current, contentDescription = contentDescription, tint = fg, modifier = Modifier.size(size * 0.46f))
        }
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
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
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
        Icon(icon, contentDescription = null, tint = pal.deepOf(tone), modifier = Modifier.padding(top = 1.dp).size(19.dp))
        Text(text, fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.9.sp, color = pal.ink)
    }
}

/** Rider count row: tinted icon box, label + discount note, − count +. */
@Composable
fun PamStepper(
    icon: ImageVector,
    tone: PamTone,
    label: String,
    chip: String?,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0,
    max: Int = 8,
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
        StepperButton(
            ph.jeepfare.ui.theme.PamIcons.Remove,
            ph.jeepfare.ui.Strings.STEPPER_DECREASE.replace("%s", label),
            enabled = value > min,
        ) { onValueChange(value - 1) }
        // The count rolls in the direction it was changed, so a tap on + reads
        // as the number being pushed up rather than replaced.
        AnimatedContent(
            targetState = value,
            transitionSpec = { pamCounterSwap(increasing = targetState > initialState) },
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(34.dp).clipToBounds(),
            label = "stepperCount",
        ) { count ->
            Text(
                "$count",
                fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = pal.ink,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
        }
        StepperButton(
            ph.jeepfare.ui.theme.PamIcons.Add,
            ph.jeepfare.ui.Strings.STEPPER_INCREASE.replace("%s", label),
            enabled = value < max,
        ) { onValueChange(value + 1) }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, contentDescription: String, enabled: Boolean, onClick: () -> Unit) {
    val pal = LocalPamPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = pressScale(pressed && enabled, 0.94f)
    val alpha = enabledAlpha(enabled)
    val bg by animateColorAsState(
        if (pressed && enabled) pal.bg2 else pal.surface,
        animationSpec = PamMotion.press(),
        label = "stepperButtonBg",
    )
    val tint by animateColorAsState(
        if (enabled) pal.ink else pal.ink3,
        animationSpec = PamMotion.quick(),
        label = "stepperButtonTint",
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .size(44.dp)
            .background(bg, CircleShape)
            .border(PamBorderWidth, pal.line2, CircleShape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

data class PamChoiceItem<T>(
    val value: T,
    val label: String,
    val icon: ImageVector,
    val tone: PamTone = PamTone.BLUE,
    val note: String? = null,
)

/**
 * Single-select tile grid — "which one am I?" rather than "how many of each".
 * Tiles wrap into rows of [columns]; the selected one fills with its signage
 * tint and takes a deep border, unselected tiles sit on the cream track color.
 */
@Composable
fun <T> PamChoiceGrid(
    items: List<PamChoiceItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { item ->
                    PamChoiceTile(
                        item = item,
                        selected = item.value == selected,
                        onClick = { onSelect(item.value) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keep a short final row aligned with the columns above it.
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun <T> PamChoiceTile(
    item: PamChoiceItem<T>,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale = pressScale(pressed, 0.97f)
    val shape = RoundedCornerShape(16.dp)

    // Selection fills and outlines blend rather than flip, and the chosen tile
    // settles a hair larger — the "this one is mine" cue reads without a bounce.
    val fill by animateColorAsState(
        if (selected) pal.tintOf(item.tone) else pal.bg2,
        animationSpec = PamMotion.standard(), label = "tileFill",
    )
    val outline by animateColorAsState(
        if (selected) pal.deepOf(item.tone) else pal.line,
        animationSpec = PamMotion.standard(), label = "tileOutline",
    )
    val accent by animateColorAsState(
        if (selected) pal.deepOf(item.tone) else pal.ink3,
        animationSpec = PamMotion.standard(), label = "tileAccent",
    )
    val labelColor by animateColorAsState(
        if (selected) pal.ink else pal.ink2,
        animationSpec = PamMotion.standard(), label = "tileLabel",
    )
    val selectScale by animateFloatAsState(
        if (selected) 1f else 0.985f,
        animationSpec = PamMotion.bouncy(), label = "tileSelectScale",
    )
    val iconScale by animateFloatAsState(
        if (selected) 1.08f else 1f,
        animationSpec = PamMotion.bouncy(), label = "tileIconScale",
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale * selectScale
                scaleY = pressScale * selectScale
            }
            .height(60.dp)
            .background(fill, shape)
            .border(PamBorderWidth, outline, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier
                .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                .size(22.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                item.label,
                fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp,
                color = labelColor, maxLines = 1,
            )
            if (item.note != null) {
                Text(
                    item.note,
                    fontFamily = fonts.body, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                    color = accent, maxLines = 1,
                )
            }
        }
    }
}

data class PamSegmentItem<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val iconTone: PamTone = PamTone.RED,
    val sub: String? = null,
)

/**
 * Cream track with a white selected pill.
 *
 * The pill is a single element that *slides* between segments instead of one
 * background switching off while another switches on — the difference between
 * a control that moves and a control that redraws.
 */
@Composable
fun <T> PamSegmented(
    items: List<PamSegmentItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    if (items.isEmpty()) return

    val gap = 4.dp
    val rowHeight = if (items.any { it.sub != null }) 52.dp else 44.dp
    val selectedIndex = items.indexOfFirst { it.value == selected }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(pal.bg2, CircleShape)
            .border(PamBorderWidth, pal.line, CircleShape)
            .padding(4.dp),
    ) {
        // Fixed widths (not weights) so the travelling pill and the labels are
        // measured from the same arithmetic and can never drift apart.
        val itemWidth = (maxWidth - gap * (items.size - 1)) / items.size
        val pillOffset by animateDpAsState(
            targetValue = (itemWidth + gap) * selectedIndex,
            animationSpec = PamMotion.spatial(),
            label = "segmentPill",
        )

        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(itemWidth)
                .height(rowHeight)
                .graphicsLayer { shadowElevation = 1.dp.toPx(); shape = CircleShape; clip = false }
                .background(pal.surface, CircleShape)
                .border(PamBorderWidth, pal.line, CircleShape),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            items.forEachIndexed { index, item ->
                val sel = index == selectedIndex
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale = pressScale(pressed, 0.97f)
                val iconTint by animateColorAsState(
                    if (sel) pal.baseOf(item.iconTone) else pal.ink3,
                    animationSpec = PamMotion.standard(), label = "segmentIcon",
                )
                val labelColor by animateColorAsState(
                    if (sel) pal.ink else pal.ink2,
                    animationSpec = PamMotion.standard(), label = "segmentLabel",
                )
                Column(
                    modifier = Modifier
                        .width(itemWidth)
                        .height(rowHeight)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        // No ripple: the design system's press feedback is fill/scale only.
                        .clickable(interactionSource = interaction, indication = null) { onSelect(item.value) },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.icon != null) {
                            Icon(
                                item.icon, contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            item.label,
                            fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            color = labelColor, maxLines = 1,
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
