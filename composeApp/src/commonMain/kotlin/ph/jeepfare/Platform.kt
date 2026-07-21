package ph.jeepfare

import androidx.compose.runtime.Composable

/** Returns a function that opens the platform share sheet with plain text. */
@Composable
expect fun rememberShareText(): (String) -> Unit

/** Short localized date-time label for the receipt, e.g. "Hul 21, 2026 · 9:41 AM". */
expect fun currentDateLabel(): String
