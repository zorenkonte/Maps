package ph.jeepfare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

/** Friendly-rounded radii (tokens/effects.css): 10 / 14 / 20 / 26, pills 999. */
val PamShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

/** Border width used on cards/controls sitting on cream (tokens/effects.css --bw). */
val PamBorderWidth = 1.5.dp

@Composable
fun PamasaheTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val palette = if (darkTheme) PamDarkPalette else PamLightPalette
    val fonts = rememberPamFonts()
    CompositionLocalProvider(
        LocalPamPalette provides palette,
        LocalPamFonts provides fonts,
    ) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(),
            typography = pamTypography(fonts),
            shapes = PamShapes,
            content = content,
        )
    }
}
