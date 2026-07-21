package ph.jeepfare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ph.jeepfare.resources.Res
import ph.jeepfare.resources.baloo2_bold
import ph.jeepfare.resources.baloo2_extrabold
import ph.jeepfare.resources.baloo2_semibold
import ph.jeepfare.resources.courierprime_bold
import ph.jeepfare.resources.courierprime_regular
import ph.jeepfare.resources.nunito_bold
import ph.jeepfare.resources.nunito_extrabold
import ph.jeepfare.resources.nunito_regular
import ph.jeepfare.resources.nunito_semibold

/**
 * Pamasahe type system (tokens/typography.css):
 * Baloo 2 for titles/buttons/big numbers, Nunito for body, Courier Prime for money math.
 * Scale: hero 34, title 22, lg 17, body 15, sm 13, xs 11.5.
 */
@Immutable
data class PamFonts(
    val display: FontFamily,
    val body: FontFamily,
    val mono: FontFamily,
)

val LocalPamFonts = staticCompositionLocalOf<PamFonts> {
    PamFonts(FontFamily.Default, FontFamily.Default, FontFamily.Monospace)
}

@Composable
fun rememberPamFonts(): PamFonts = PamFonts(
    display = FontFamily(
        Font(Res.font.baloo2_semibold, FontWeight.SemiBold),
        Font(Res.font.baloo2_bold, FontWeight.Bold),
        Font(Res.font.baloo2_extrabold, FontWeight.ExtraBold),
    ),
    body = FontFamily(
        Font(Res.font.nunito_regular, FontWeight.Normal),
        Font(Res.font.nunito_semibold, FontWeight.SemiBold),
        Font(Res.font.nunito_bold, FontWeight.Bold),
        Font(Res.font.nunito_extrabold, FontWeight.ExtraBold),
    ),
    mono = FontFamily(
        Font(Res.font.courierprime_regular, FontWeight.Normal),
        Font(Res.font.courierprime_bold, FontWeight.Bold),
    ),
)

/** Overline style: 11.5px, 800, caps, +0.06em tracking (rendered via uppercase text). */
fun PamFonts.overline() = TextStyle(
    fontFamily = body,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 11.5.sp,
    letterSpacing = 0.06.em,
)

fun pamTypography(fonts: PamFonts): Typography = Typography(
    displayLarge = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 37.4.sp),
    headlineMedium = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 25.3.sp),
    headlineSmall = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 22.sp),
    titleLarge = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.1.sp),
    bodyLarge = TextStyle(fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.1.sp),
    bodyMedium = TextStyle(fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.5.sp),
    bodySmall = TextStyle(fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.2.sp),
    labelLarge = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 15.sp),
    labelMedium = TextStyle(fontFamily = fonts.body, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.2.sp),
    labelSmall = TextStyle(fontFamily = fonts.body, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, lineHeight = 15.5.sp, letterSpacing = 0.06.em),
)
