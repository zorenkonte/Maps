package ph.jeepfare.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

/**
 * Scrims androidx applies where the icons themselves cannot be re-tinted — the
 * navigation bar below API 27. Same values `enableEdgeToEdge` uses by default.
 */
private val LightScrim = 0xE6FFFFFF.toInt()
private val DarkScrim = 0x801B1B1B.toInt()

@Composable
actual fun PamSystemBars(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val activity = view.context.findComponentActivity() ?: return
    SideEffect {
        // Re-applied rather than set once in the activity: `auto` decides the
        // icon tint from the callback, so handing it the app's own theme is what
        // keeps the bars in step with the in-app toggle. Both bars stay
        // transparent — the page colour already runs edge to edge behind them.
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme },
        )
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
