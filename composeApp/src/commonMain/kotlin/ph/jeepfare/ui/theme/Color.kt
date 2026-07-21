package ph.jeepfare.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Pamasahe color tokens — jeepney folk-art: bold red/yellow/blue signage accents
 * on warm cream, chrome trim. Mirrors tokens/colors.css in the design project.
 */
@Immutable
data class PamPalette(
    val red: Color, val redDeep: Color, val redTint: Color,
    val yellow: Color, val yellowDeep: Color, val yellowTint: Color,
    val blue: Color, val blueDeep: Color, val blueTint: Color,
    val green: Color, val greenDeep: Color, val greenTint: Color,
    val bg: Color, val bg2: Color, val surface: Color, val surface2: Color,
    val ink: Color, val ink2: Color, val ink3: Color,
    val line: Color, val line2: Color,
    val isDark: Boolean,
) {
    // Semantic aliases (tokens/colors.css)
    val action: Color get() = red
    val actionInk: Color get() = Color.White
    val actionPress: Color get() = redDeep
    val focus: Color get() = blue
    val ok: Color get() = green
}

val PamLightPalette = PamPalette(
    red = Color(0xFFC41E2F), redDeep = Color(0xFF921322), redTint = Color(0xFFFBE7E4),
    yellow = Color(0xFFF2A900), yellowDeep = Color(0xFFA87500), yellowTint = Color(0xFFFFF3D0),
    blue = Color(0xFF1D5FBF), blueDeep = Color(0xFF123F85), blueTint = Color(0xFFE4EDFB),
    green = Color(0xFF1B8A5A), greenDeep = Color(0xFF0F6B43), greenTint = Color(0xFFDFF2E7),
    bg = Color(0xFFFAF5EC), bg2 = Color(0xFFF2EADB), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFFFFBF1),
    ink = Color(0xFF271F18), ink2 = Color(0xFF6D6156), ink3 = Color(0xFF9C8F80),
    line = Color(0xFFEAE0CE), line2 = Color(0xFFD6C9B0),
    isDark = false,
)

val PamDarkPalette = PamPalette(
    red = Color(0xFFE4545A), redDeep = Color(0xFFC43840), redTint = Color(0x29E4545A),
    yellow = Color(0xFFFFC53D), yellowDeep = Color(0xFFE5A81F), yellowTint = Color(0x24FFC53D),
    blue = Color(0xFF6FA3EE), blueDeep = Color(0xFF4C86DC), blueTint = Color(0x296FA3EE),
    green = Color(0xFF3FBF87), greenDeep = Color(0xFF2FA371), greenTint = Color(0x263FBF87),
    bg = Color(0xFF191410), bg2 = Color(0xFF131009), surface = Color(0xFF251E16), surface2 = Color(0xFF2C241B),
    ink = Color(0xFFF7EFE1), ink2 = Color(0xFFC7B9A5), ink3 = Color(0xFF92856E),
    line = Color(0xFF3A3226), line2 = Color(0xFF4B4030),
    isDark = true,
)

val LocalPamPalette = staticCompositionLocalOf { PamLightPalette }

/** Signage tint tones used by chips, callouts, and stepper icon boxes. */
enum class PamTone { RED, YELLOW, BLUE, GREEN, NEUTRAL }

fun PamPalette.tintOf(tone: PamTone): Color = when (tone) {
    PamTone.RED -> redTint
    PamTone.YELLOW -> yellowTint
    PamTone.BLUE -> blueTint
    PamTone.GREEN -> greenTint
    PamTone.NEUTRAL -> bg2
}

fun PamPalette.baseOf(tone: PamTone): Color = when (tone) {
    PamTone.RED -> red
    PamTone.YELLOW -> yellow
    PamTone.BLUE -> blue
    PamTone.GREEN -> green
    PamTone.NEUTRAL -> ink2
}

fun PamPalette.deepOf(tone: PamTone): Color = when (tone) {
    PamTone.RED -> if (isDark) red else red // red uses base hue for text per design
    PamTone.YELLOW -> yellowDeep
    PamTone.BLUE -> blueDeep
    PamTone.GREEN -> greenDeep
    PamTone.NEUTRAL -> ink2
}

/** readme.md mapping: primary=red, secondary=blue, tertiary=yellow, outline=line-2. */
fun PamPalette.toColorScheme(): ColorScheme = if (isDark) {
    darkColorScheme(
        primary = red, onPrimary = actionInk,
        secondary = blue, onSecondary = Color.White,
        tertiary = yellow, onTertiary = Color(0xFF271F18),
        background = bg, onBackground = ink,
        surface = surface, onSurface = ink,
        surfaceVariant = surface2, onSurfaceVariant = ink2,
        outline = line2, outlineVariant = line,
        primaryContainer = redTint, onPrimaryContainer = ink,
        secondaryContainer = blueTint, onSecondaryContainer = ink,
        tertiaryContainer = yellowTint, onTertiaryContainer = ink,
        error = red, onError = Color.White,
    )
} else {
    lightColorScheme(
        primary = red, onPrimary = actionInk,
        secondary = blue, onSecondary = Color.White,
        tertiary = yellow, onTertiary = Color(0xFF271F18),
        background = bg, onBackground = ink,
        surface = surface, onSurface = ink,
        surfaceVariant = surface2, onSurfaceVariant = ink2,
        outline = line2, outlineVariant = line,
        primaryContainer = redTint, onPrimaryContainer = redDeep,
        secondaryContainer = blueTint, onSecondaryContainer = blueDeep,
        tertiaryContainer = yellowTint, onTertiaryContainer = yellowDeep,
        error = red, onError = Color.White,
    )
}
