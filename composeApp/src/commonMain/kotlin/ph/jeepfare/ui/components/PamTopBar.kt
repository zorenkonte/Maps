package ph.jeepfare.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.jeepfare.ui.Strings
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamBorderWidth
import ph.jeepfare.ui.theme.PamIcons
import ph.jeepfare.ui.theme.PamMotion
import ph.jeepfare.ui.theme.pamEnter

/** Height of the pinned bar: a 48dp action with a little air either side. */
val PamBarHeight: Dp = 56.dp

/**
 * Eases [value] over the window [from]..[to], flat at 0 before it and 1 after.
 *
 * The bar's two jobs — taking on the page colour, and showing its own title —
 * happen at different points of the same collapse, so each reads the scroll
 * through its own window instead of tracking it one-to-one.
 */
private fun ramp(value: Float, from: Float, to: Float): Float =
    PamMotion.Easing.transform(((value - from) / (to - from)).coerceIn(0f, 1f))

/**
 * A header that stays put while the page moves under it, the way an iOS
 * navigation bar does.
 *
 * At rest the bar is invisible — the page colour runs straight up behind it. As
 * soon as content passes underneath, it takes on the page colour and a hairline
 * so nothing scrolls out into the status bar unframed. [largeHeader], when
 * given, is the big title that lives in the scroll content: it slides away and
 * hands over to the bar's own compact title, which is what [bar] gets the
 * collapse fraction for (0 while the large header is fully on screen, 1 once it
 * is gone; a screen with no large header is simply collapsed the moment it
 * scrolls).
 *
 * The fraction is passed as a lambda rather than a value on purpose: read it
 * inside `graphicsLayer`/`drawBehind` and a scroll only re-draws the bar, where
 * reading it in composition would recompose the whole page every frame.
 */
@Composable
fun PamPinnedHeader(
    bar: @Composable RowScope.(collapse: () -> Float) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    largeHeader: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val pal = LocalPamPalette.current
    // Measured height of the large header, so the hand-over lands exactly when
    // the big title has gone behind the bar rather than at a guessed offset.
    val largeHeaderHeight = remember { mutableFloatStateOf(0f) }
    val collapse: () -> Float = remember(scrollState) {
        {
            val scrolled = scrollState.value.toFloat()
            val height = largeHeaderHeight.floatValue
            when {
                scrolled <= 0f -> 0f
                height <= 0f -> 1f
                else -> (scrolled / height).coerceIn(0f, 1f)
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
            // The page starts below the bar; from there on it scrolls under it.
            Spacer(Modifier.height(PamBarHeight))
            if (largeHeader != null) {
                Box(
                    Modifier
                        .onSizeChanged { largeHeaderHeight.floatValue = it.height.toFloat() }
                        // It fades as it goes, so it is already gone by the time
                        // the bar's own title arrives — never two titles at once.
                        .graphicsLayer { alpha = 1f - ramp(collapse(), 0f, 0.75f) },
                ) {
                    largeHeader()
                }
            }
            content()
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(PamBarHeight)
                .drawBehind {
                    val shown = ramp(collapse(), 0.55f, 1f)
                    if (shown <= 0f) return@drawBehind
                    drawRect(pal.bg.copy(alpha = shown))
                    val hairline = PamBorderWidth.toPx()
                    drawRect(
                        color = pal.line.copy(alpha = shown),
                        topLeft = Offset(0f, size.height - hairline),
                        size = Size(size.width, hairline),
                    )
                }
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bar(collapse)
        }
    }
}

/**
 * Title inside the pinned bar.
 *
 * With a [collapse] fraction it belongs to a screen that has a large header, so
 * it stays out of the way until that header has left and then rises into its
 * place; without one the screen has no big title to defer to and the bar simply
 * carries the name all along.
 */
@Composable
fun RowScope.PamBarTitle(
    title: String,
    modifier: Modifier = Modifier,
    collapse: (() -> Float)? = null,
    mark: Boolean = false,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = modifier
            .weight(1f)
            .then(
                if (collapse == null) {
                    Modifier
                } else {
                    Modifier.graphicsLayer {
                        val shown = ramp(collapse(), 0.6f, 1f)
                        alpha = shown
                        translationY = (1f - shown) * 10.dp.toPx()
                    }
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mark) PamJeepMark(size = 26.dp, radius = 9.dp, stripeHeight = 2.5.dp)
        Text(
            title,
            fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 18.sp,
            color = pal.ink, maxLines = 1,
        )
    }
}

/** The red jeepney mark with its stripe trim, at whatever size it is asked for. */
@Composable
fun PamJeepMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    radius: Dp = 14.dp,
    stripeHeight: Dp = 4.dp,
) {
    val pal = LocalPamPalette.current
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(radius)).background(pal.red),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            PamIcons.AirportShuttle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f),
        )
        PamStripe(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            height = stripeHeight, segment = size * 0.16f,
        )
    }
}

/**
 * Hero header: the jeepney mark, the Baloo wordmark and the tagline, sized like
 * an iOS large title — it lives at the top of the page and scrolls away.
 */
@Composable
fun PamHeroTopBar(modifier: Modifier = Modifier) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PamJeepMark(size = 44.dp)
        Column(Modifier.weight(1f)) {
            Text(
                Strings.APP_TITLE,
                fontFamily = fonts.display, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, lineHeight = 30.sp,
                color = pal.ink,
            )
            Text(
                Strings.APP_TAGLINE,
                fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp,
                color = pal.ink2,
            )
        }
    }
}

/** Back action for the pinned bar, entering with the rest of the screen. */
@Composable
fun PamBarBack(onBack: () -> Unit) {
    PamIconButton(
        PamIcons.ArrowBack,
        contentDescription = Strings.BACK,
        onClick = onBack,
        modifier = Modifier.pamEnter(index = 0),
    )
}
