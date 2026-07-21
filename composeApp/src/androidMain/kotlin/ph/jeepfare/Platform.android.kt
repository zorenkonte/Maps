package ph.jeepfare

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun rememberShareText(): (String) -> Unit {
    val context = LocalContext.current
    return { text ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}

actual fun currentDateLabel(): String {
    val fil = Locale.forLanguageTag("fil-PH")
    val locale = if (fil.language.isNotEmpty()) fil else Locale.getDefault()
    return SimpleDateFormat("MMM d, yyyy · h:mm a", locale).format(Date())
}
