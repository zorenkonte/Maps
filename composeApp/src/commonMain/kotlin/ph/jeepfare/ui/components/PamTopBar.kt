package ph.jeepfare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.jeepfare.ui.Strings
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamIcons

/** Hero header: red jeepney mark with thin stripe trim, Baloo wordmark, tagline. */
@Composable
fun PamHeroTopBar(trailing: @Composable () -> Unit = {}) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(pal.red),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PamIcons.AirportShuttle, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            PamStripe(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                height = 3.5.dp, segment = 6.dp,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                Strings.APP_TITLE,
                fontFamily = fonts.display, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 22.sp,
                color = pal.ink,
            )
            Text(
                Strings.APP_TAGLINE,
                fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                color = pal.ink2,
            )
        }
        trailing()
    }
}

/** Standard header: back button, Baloo title, trailing action. */
@Composable
fun PamTopBar(title: String, onBack: (() -> Unit)? = null, trailing: @Composable () -> Unit = {}) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            PamIconButton(PamIcons.ArrowBack, contentDescription = Strings.BACK, onClick = onBack)
        }
        Text(
            title,
            fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 19.sp,
            color = pal.ink,
            modifier = Modifier.weight(1f).padding(start = if (onBack == null) 6.dp else 0.dp),
        )
        trailing()
    }
}
