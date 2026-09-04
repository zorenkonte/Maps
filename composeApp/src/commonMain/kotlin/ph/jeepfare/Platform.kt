package ph.jeepfare

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Returns a function that opens the platform share sheet with [image] as a PNG.
 *
 * A receipt is something you show someone, so it travels as a picture: it lands
 * in a chat looking like the receipt on screen, with no app needed to read it.
 * [fileName] is the base name (no extension) the receiving app sees.
 */
@Composable
expect fun rememberShareImage(): (image: ImageBitmap, fileName: String) -> Unit

/**
 * Returns a function that writes [image] to the device's photo gallery as a PNG.
 *
 * [onResult] receives false when the save was refused (no permission) or failed,
 * so the button can say so instead of claiming a save that never happened.
 */
@Composable
expect fun rememberSaveImage(): (image: ImageBitmap, fileName: String, onResult: (Boolean) -> Unit) -> Unit

/** Short English date-time label for the receipt, e.g. "Jul 21, 2026 · 9:41 AM". */
expect fun currentDateLabel(): String
