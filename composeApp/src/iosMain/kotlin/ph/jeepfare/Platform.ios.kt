package ph.jeepfare

import androidx.compose.runtime.Composable
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPopoverArrowDirectionAny

@Composable
actual fun rememberShareText(): (String) -> Unit = { text ->
    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
    if (rootController != null) {
        val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        // On iPad the share sheet is a popover and crashes without an anchor;
        // anchor it to the center of the root view.
        controller.popoverPresentationController?.let { popover ->
            rootController.view.let { view ->
                popover.sourceView = view
                val (cx, cy) = view.bounds.useContents { size.width / 2 to size.height / 2 }
                popover.sourceRect = CGRectMake(cx, cy, 0.0, 0.0)
                popover.permittedArrowDirections = UIPopoverArrowDirectionAny
            }
        }
        rootController.presentViewController(controller, animated = true, completion = null)
    }
}

actual fun currentDateLabel(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "MMM d, yyyy · h:mm a"
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromDate(NSDate())
}
