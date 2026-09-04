package ph.jeepfare.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyleLight

@Composable
actual fun PamSystemBars(darkTheme: Boolean) {
    LaunchedEffect(darkTheme) {
        // The status bar takes its colour from the window's interface style, so
        // pinning that to the app's theme is what turns the clock and the
        // battery dark over a light page. It also carries into anything UIKit
        // puts on top of us — the share sheet included.
        UIApplication.sharedApplication.keyWindow?.overrideUserInterfaceStyle =
            if (darkTheme) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
    }
}
