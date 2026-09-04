package ph.jeepfare.ui.theme

import androidx.compose.runtime.Composable

/**
 * Keeps the platform's status- and navigation-bar icons legible against the
 * app's own theme.
 *
 * The theme is the app's to decide — the header toggle overrides the system
 * setting — so the bars cannot be left following the system: a light app under
 * a dark system draws white status icons onto cream and they all but vanish.
 */
@Composable
expect fun PamSystemBars(darkTheme: Boolean)
