package ph.jeepfare

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Subdirectory of the gallery the receipts land in. */
private const val GALLERY_FOLDER = "Pamasahe"

/** Cache subdirectory backing the share provider; see res/xml/file_paths.xml. */
private const val SHARE_CACHE_DIR = "receipts"

@Composable
actual fun rememberShareImage(): (ImageBitmap, String) -> Unit {
    val context = LocalContext.current
    return { image, fileName ->
        // Written to the cache and handed over through the FileProvider: the
        // receiving app gets a temporary read grant, so sharing needs no
        // storage permission of our own.
        val file = File(context.cacheDir, SHARE_CACHE_DIR).let { dir ->
            dir.mkdirs()
            File(dir, "$fileName.png")
        }
        FileOutputStream(file).use { out ->
            image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}

/** A save waiting on the storage permission prompt (Android 9 and older). */
private class PendingSave(
    val image: ImageBitmap,
    val fileName: String,
    val onResult: (Boolean) -> Unit,
)

@Composable
actual fun rememberSaveImage(): (ImageBitmap, String, (Boolean) -> Unit) -> Unit {
    val context = LocalContext.current
    val pending = remember { mutableStateOf<PendingSave?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val save = pending.value
        pending.value = null
        if (save != null) {
            save.onResult(granted && writeToGallery(context, save.image, save.fileName))
        }
    }
    return { image, fileName, onResult ->
        if (canWriteGallery(context)) {
            onResult(writeToGallery(context, image, fileName))
        } else {
            // Android 9 and older have no scoped MediaStore write, so the save
            // has to wait for the permission answer before it can run.
            pending.value = PendingSave(image, fileName, onResult)
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}

private fun canWriteGallery(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
        PackageManager.PERMISSION_GRANTED

private fun writeToGallery(context: Context, image: ImageBitmap, fileName: String): Boolean {
    val bitmap = image.asAndroidBitmap()
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(context, bitmap, fileName)
        } else {
            writeToPublicPictures(context, bitmap, fileName)
        }
    } catch (e: Exception) {
        // A failed save is reported to the caller, never crashed on: the phone
        // may be out of space, or the gallery provider may refuse the insert.
        false
    }
}

private fun writeViaMediaStore(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$GALLERY_FOLDER")
        // Kept out of the gallery until the bytes are actually there.
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    val written = resolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    } ?: false
    if (!written) {
        resolver.delete(uri, null, null)
        return false
    }
    values.clear()
    values.put(MediaStore.Images.Media.IS_PENDING, 0)
    resolver.update(uri, values, null, null)
    return true
}

private fun writeToPublicPictures(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        GALLERY_FOLDER,
    )
    if (!dir.exists() && !dir.mkdirs()) return false
    val file = File(dir, "$fileName.png")
    val written = FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    if (!written) return false
    // Without a scan the file is on disk but never appears in the gallery.
    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
    return true
}

actual fun currentDateLabel(): String =
    // The UI is English-only, so the receipt date is formatted in English too
    // rather than in the device locale.
    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.ENGLISH).format(Date())
