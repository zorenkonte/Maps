package ph.jeepfare

import androidx.compose.runtime.Composable
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun rememberShareText(): (String) -> Unit = { text ->
    val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
        controller, animated = true, completion = null,
    )
}

actual fun currentDateLabel(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "MMM d, yyyy · h:mm a"
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromDate(NSDate())
}
