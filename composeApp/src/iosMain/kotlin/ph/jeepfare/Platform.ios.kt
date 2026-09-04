package ph.jeepfare

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UIPopoverArrowDirectionAny

@Composable
actual fun rememberShareImage(): (ImageBitmap, String) -> Unit = { image, _ ->
    // UIActivityViewController takes the UIImage itself — no temporary file and
    // no file provider, and Photos/Messages treat it as a real picture.
    image.toUIImage()?.let { presentShareSheet(it) }
}

@Composable
actual fun rememberSaveImage(): (ImageBitmap, String, (Boolean) -> Unit) -> Unit = { image, _, onResult ->
    val uiImage = image.toUIImage()
    if (uiImage == null) {
        onResult(false)
    } else {
        // The system asks for photo-library access on first use (see
        // NSPhotoLibraryAddUsageDescription in Info.plist) and reports failure
        // only through a completion selector, which Kotlin cannot supply — so a
        // refusal shows up as the picture simply not being in Photos.
        UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)
        onResult(true)
    }
}

private fun presentShareSheet(image: UIImage) {
    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    val controller = UIActivityViewController(activityItems = listOf(image), applicationActivities = null)
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

/** Encodes the captured receipt as PNG and wraps it in a UIImage. */
@OptIn(ExperimentalForeignApi::class)
private fun ImageBitmap.toUIImage(): UIImage? {
    val png = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG) ?: return null
    val bytes = png.bytes
    if (bytes.isEmpty()) return null
    val data = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    return UIImage.imageWithData(data)
}

actual fun currentDateLabel(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "MMM d, yyyy · h:mm a"
        // English-only UI: don't follow the device locale.
        locale = NSLocale(localeIdentifier = "en_US")
    }
    return formatter.stringFromDate(NSDate())
}
