package ph.jeepfare.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Motion tokens for Pamasahe.
 *
 * The design system's feel is "quick and physical": nothing crossfades lazily,
 * nothing bounces like a toy. Presses use a short eased tween so finger feedback
 * is immediate; anything that *moves* — a segmented pill, a sliding screen, a
 * receipt arriving — uses a well-damped spring so it settles naturally instead
 * of stopping dead at the end of a fixed duration.
 *
 * Every animation in the app pulls its spec from here, so changing the feel is
 * one edit rather than a hunt through screens.
 */
object PamMotion {
    /** cubic-bezier(.2,.7,.3,1) — the design system's standard ease-out. */
    val Easing = CubicBezierEasing(0.2f, 0.7f, 0.3f, 1f)

    /** Press / release feedback. Short enough to read as "instant". */
    const val PressMs = 160

    /** Color, alpha and other non-spatial state changes. */
    const val QuickMs = 180
    const val StandardMs = 260

    /** Entrance travel distance — small, so content reads as settling, not flying. */
    val EnterOffset: Dp = 14.dp

    /** Spatial motion: pills, panels, screens. */
    fun <T> spatial(): SpringSpec<T> =
        spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)

    /** A touch livelier — for a small element popping into place. */
    fun <T> bouncy(): SpringSpec<T> =
        spring(dampingRatio = 0.68f, stiffness = Spring.StiffnessMedium)

    /** Non-spatial fades and color blends. */
    fun <T> quick(): TweenSpec<T> = tween(durationMillis = QuickMs, easing = Easing)

    fun <T> standard(): TweenSpec<T> = tween(durationMillis = StandardMs, easing = Easing)

    /** Press-scale feedback, matching the design system's 160ms ease. */
    fun <T> press(): TweenSpec<T> = tween(durationMillis = PressMs, easing = Easing)
}

/**
 * Content swap for one block replacing another in place (a distance readout, a
 * tab body, a loading state): the outgoing block fades out fast, the incoming
 * one fades and rises into its place.
 */
fun pamSwap(upward: Boolean = true): ContentTransform {
    val direction = if (upward) 1 else -1
    return (
        fadeIn(animationSpec = tween(PamMotion.StandardMs, delayMillis = 40, easing = PamMotion.Easing)) +
            slideInVertically(animationSpec = PamMotion.spatial()) { height -> height / 6 * direction }
        ) togetherWith fadeOut(animationSpec = tween(120, easing = PamMotion.Easing))
}

/** Number swap inside a tight row — travels along the direction of the change. */
fun pamCounterSwap(increasing: Boolean): ContentTransform =
    (
        slideInVertically(animationSpec = PamMotion.spatial()) { h -> if (increasing) h else -h } +
            fadeIn(animationSpec = PamMotion.quick())
        ) togetherWith (
        slideOutVertically(animationSpec = PamMotion.spatial()) { h -> if (increasing) -h else h } +
            fadeOut(animationSpec = tween(100, easing = PamMotion.Easing))
        )

/**
 * Glyph swap inside a fixed-size control (the sun/moon theme toggle): the old
 * icon shrinks away as the new one turns in, so the button never blinks.
 */
fun pamIconSwap(): ContentTransform =
    (
        fadeIn(animationSpec = PamMotion.quick()) +
            scaleIn(initialScale = 0.6f, animationSpec = PamMotion.bouncy())
        ) togetherWith (
        fadeOut(animationSpec = tween(120, easing = PamMotion.Easing)) +
            scaleOut(targetScale = 0.6f, animationSpec = tween(120, easing = PamMotion.Easing))
        )

/**
 * Screen-to-screen transition. [forward] pushes the new screen in from the
 * trailing edge while the outgoing one drifts back and dims — the depth cue a
 * stack navigation gives you for free on either platform.
 */
fun <S> AnimatedContentTransitionScope<S>.pamScreenTransform(forward: Boolean): ContentTransform {
    val towards = if (forward) {
        AnimatedContentTransitionScope.SlideDirection.Start
    } else {
        AnimatedContentTransitionScope.SlideDirection.End
    }
    return (
        slideIntoContainer(towards, animationSpec = PamMotion.spatial()) +
            fadeIn(animationSpec = tween(PamMotion.StandardMs, easing = PamMotion.Easing)) +
            scaleIn(initialScale = 0.97f, animationSpec = PamMotion.spatial())
        ) togetherWith (
        slideOutOfContainer(towards, animationSpec = PamMotion.spatial()) +
            fadeOut(animationSpec = tween(PamMotion.StandardMs, easing = PamMotion.Easing)) +
            scaleOut(targetScale = 0.97f, animationSpec = PamMotion.spatial())
        )
}

/** A block that unfolds in place — companion steppers, inline notes. */
fun pamRevealEnter(): EnterTransition =
    expandVertically(animationSpec = PamMotion.spatial(), expandFrom = Alignment.Top) +
        fadeIn(animationSpec = tween(PamMotion.StandardMs, delayMillis = 60, easing = PamMotion.Easing))

fun pamRevealExit(): ExitTransition =
    shrinkVertically(animationSpec = PamMotion.spatial(), shrinkTowards = Alignment.Top) +
        fadeOut(animationSpec = tween(110, easing = PamMotion.Easing))

/** Something that arrives as a finished object — the resibo. */
fun pamArriveEnter(): EnterTransition =
    fadeIn(animationSpec = tween(PamMotion.StandardMs, easing = PamMotion.Easing)) +
        slideInVertically(animationSpec = PamMotion.spatial()) { h -> h / 5 } +
        scaleIn(initialScale = 0.96f, animationSpec = PamMotion.spatial())

fun pamArriveExit(): ExitTransition =
    fadeOut(animationSpec = tween(120, easing = PamMotion.Easing)) +
        scaleOut(targetScale = 0.98f, animationSpec = tween(120, easing = PamMotion.Easing))

/** Slide-up entrance for a panel anchored to the bottom of the screen. */
fun pamPanelEnter(): EnterTransition =
    slideInVertically(animationSpec = PamMotion.spatial()) { h -> h } +
        fadeIn(animationSpec = tween(PamMotion.StandardMs, easing = PamMotion.Easing))

fun pamPanelExit(): ExitTransition =
    slideOutVertically(animationSpec = PamMotion.spatial()) { h -> h } +
        fadeOut(animationSpec = tween(140, easing = PamMotion.Easing))

/**
 * One-shot entrance for content that is already there when a screen opens: it
 * fades up into place, staggered by [index] so a column of cards arrives as a
 * sequence rather than a slab.
 *
 * It plays once per composition of the caller — recomposing for new data, or
 * switching a tab inside the card, does not replay it.
 */
@Composable
fun Modifier.pamEnter(
    index: Int = 0,
    perItemDelayMs: Int = 40,
    baseDelayMs: Int = 20,
    offset: Dp = PamMotion.EnterOffset,
): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((baseDelayMs + index * perItemDelayMs).toLong())
        progress.animateTo(1f, animationSpec = tween(320, easing = PamMotion.Easing))
    }
    // Read inside graphicsLayer so each frame only re-draws, never re-composes.
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offset.toPx()
    }
}
