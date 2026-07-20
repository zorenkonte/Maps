package ph.jeepfare.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.rememberShareText
import ph.jeepfare.currentDateLabel
import ph.jeepfare.ui.components.PamButton
import ph.jeepfare.ui.components.PamButtonVariant
import ph.jeepfare.ui.components.PamStripe
import ph.jeepfare.ui.components.PamTopBar
import ph.jeepfare.ui.components.Resibo
import ph.jeepfare.ui.components.ResiboRow
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamIcons

@Composable
fun ReceiptScreen(breakdown: FareBreakdown, onBack: () -> Unit) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val shareText = rememberShareText()
    val clipboard = LocalClipboardManager.current
    val dateLabel = remember { currentDateLabel() }
    var copied by remember { mutableStateOf(false) }

    val (rows, dividerAt) = resiboRows(breakdown)
    val allRows = listOf(ResiboRow(dateLabel, "", muted = true)) + rows
    val shiftedDivider = dividerAt?.plus(1)

    Scaffold(containerColor = pal.bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            PamTopBar(Strings.RECEIPT_TITLE, onBack = onBack)
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PamStripe(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp)),
                )
                Resibo(
                    header = Strings.RESIBO_HEADER,
                    sub = "${Strings.jeepneyTypeLong(breakdown.jeepneyType)} · ${formatKm(breakdown.distanceKm)} km",
                    rows = allRows,
                    dividerBeforeIndex = shiftedDivider,
                    totalLabel = Strings.RESIBO_TOTAL,
                    totalValue = breakdown.total.peso(),
                    footer = Strings.RESIBO_FOOTER,
                    pop = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PamButton(
                        Strings.SHARE,
                        onClick = { shareText(resiboShareText(breakdown, dateLabel)) },
                        icon = PamIcons.Share,
                        modifier = Modifier.weight(1f),
                    )
                    PamButton(
                        if (copied) Strings.COPIED else Strings.COPY,
                        onClick = {
                            clipboard.setText(AnnotatedString(resiboShareText(breakdown, dateLabel)))
                            copied = true
                        },
                        icon = PamIcons.Download,
                        variant = PamButtonVariant.SECONDARY,
                    )
                }
                Text(
                    Strings.RECEIPT_HINT,
                    fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = pal.ink2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
